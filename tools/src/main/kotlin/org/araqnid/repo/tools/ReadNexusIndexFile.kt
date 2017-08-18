package org.araqnid.repo.tools

import com.google.common.base.Splitter
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Instant
import java.util.*
import java.util.function.Consumer
import java.util.stream.Stream
import java.util.stream.StreamSupport
import java.util.zip.GZIPInputStream

object ReadNexusIndexFile {
    @JvmStatic fun main(args: Array<String>) {
        openIndexFile(Paths.get(args[0])).contentStream
                .filter { it.fields.containsKey("u") }
                .map { ArtifactInfo.from(it) }
                .filter { it.artifactId.indexOf("dropwizard") >= 0 || it.groupId.indexOf("dropwizard") > 0 }
                .forEachOrdered { println("artifact: $it") }
    }

    fun openIndexFile(filename: Path): IndexFile {
        val inputStream = DataInputStream(open(filename))
        val headerByte = inputStream.readByte()
        if (headerByte != 1.toByte()) throw IOException("Index format version mismatch, found version $headerByte")
        val timestamp = Instant.ofEpochMilli(inputStream.readLong())
        val contentStream = StreamSupport.stream(object : Spliterators.AbstractSpliterator<Document>(Long.MAX_VALUE, Spliterator.NONNULL or Spliterator.DISTINCT or Spliterator.IMMUTABLE) {
            override fun tryAdvance(action: Consumer<in Document>): Boolean {
                val document = readDocument(inputStream) ?: return false
                action.accept(document)
                return true
            }
        }, false).onClose { inputStream.close() }
        return IndexFile(timestamp, contentStream)
    }

    fun readDocument(dis: DataInputStream): Document? {
        val fieldCount = try {
            dis.readInt()
        } catch (ex: EOFException) {
            return null
        }

        val fields = LinkedHashMap<String, FieldValue>()
        for (i in 1..fieldCount) {
            val field = readField(dis)
            fields.put(field.name, FieldValue(field.flags, field.value))
        }

        val uinfoField = fields["u"] // UINFO
        val info = fields["i"]?.value // INFO
        if (uinfoField != null && info != null && !info.isEmpty()) {
            val splitInfo = Splitter.on('|').splitToList(info)
            if (splitInfo.size > 6) {
                val extension = splitInfo[6]
                val uinfoString = uinfoField.value
                if (uinfoString.endsWith("|NA")) {
                    val newFieldValue = uinfoString + "|" + (extension ?: "NA")
                    if (newFieldValue != uinfoField.value) {
                        fields["u"] = uinfoField.copy(value = newFieldValue)
                    }
                }
            }
        }
        return Document(fields)
    }


    fun readField(dis: DataInputStream): Field {
        val flags = dis.read()

        val flagSet = EnumSet.noneOf(Flag::class.java)
        if (flags and 1 != 0) flagSet.add(Flag.INDEXED)
        if (flags and 2 != 0) flagSet.add(Flag.ANALYZED)
        if (flags and 4 != 0) flagSet.add(Flag.STORED)

        val name = dis.readUTF()
        val value = dis.readUTFWithIntLength()

        return Field(flagSet, name, value)
    }

    fun DataInputStream.readUTFWithIntLength(): String {
        val utflen = readInt()
        val(bytearr, chararr) =
            try {
                val bytes = ByteArray(utflen)
                val chars = CharArray(utflen)
                Pair(bytes, chars)
            } catch (e: OutOfMemoryError) {
                throw IOException("Index data content is inappropriate (is junk?), leads to OutOfMemoryError! See MINDEXER-28 for more information!", e)
            }

        readFully(bytearr, 0, utflen)

        var count = 0
        var chararr_count = 0
        while (count < utflen) {
            val c = bytearr[count].toInt() and 0xff
            if (c > 127) break
            count++
            chararr[chararr_count++] = c.toChar()
        }
        while (count < utflen) {
            val c = bytearr[count].toInt() and 0xff
            when (c shr 4) {
                0, 1, 2, 3, 4, 5, 6, 7 -> {
                    /* 0xxxxxxx */
                    count++
                    chararr[chararr_count++] = c.toChar()
                }
                12, 13 -> {
                    /* 110x xxxx 10xx xxxx */
                    count += 2
                    if (count > utflen) throw UTFDataFormatException( "malformed input: partial character at end" )
                    val char2 = bytearr[count - 1].toInt()
                    if (char2 and 0xC0 != 0x80) {
                        throw UTFDataFormatException("malformed input around byte " + count)
                    }
                    chararr[chararr_count++] = (c and 0x1F shl 6 or (char2 and 0x3F)).toChar()
                }
                14 -> {
                    count += 3
                    if (count > utflen) {
                        throw UTFDataFormatException("malformed input: partial character at end")
                    }
                    val char2 = bytearr[count - 2].toInt()
                    val char3 = bytearr[count - 1].toInt()
                    if (char2 and 0xC0 != 0x80 || char3 and 0xC0 != 0x80) {
                        throw UTFDataFormatException("malformed input around byte " + (count - 1))
                    }
                    chararr[chararr_count++] = (c and 0x0F shl 12 or (char2 and 0x3F shl 6) or (char3 and 0x3F shl 0)).toChar()
                }
                else -> {
                    /* 10xx xxxx, 1111 xxxx */
                    throw UTFDataFormatException("malformed input around byte " + count)
                }
            }
        }

        return String(chararr, 0, chararr_count)
    }

    @Throws(IOException::class)
    fun open(path: Path): InputStream {
        val fileStream = Files.newInputStream(path)

        return when {
            path.fileName.toString().endsWith(".gz") -> GZIPInputStream(fileStream)
            else -> BufferedInputStream(fileStream)
        }
    }

    data class ArtifactInfo(val groupId: String, val artifactId: String, val version: String, val classifier: String, val packaging: String, val description: String?, val lastModified: Instant?, val sha1: String?) {
        companion object {
            fun from(doc: Document): ArtifactInfo {
                val lastModified = doc.fields["m"]?.value?.toInstantAtEpochMillis()
                val(groupId, artifactId, version, classifier, extensionOrPackaging) = Splitter.on('|').splitToList(doc.fields["u"]!!.value)
                val sha1 = doc.fields["1"]?.value
                val description = doc.fields["d"]?.value
                return ArtifactInfo(groupId, artifactId, version, classifier, extensionOrPackaging, description, lastModified, sha1)
            }
        }
    }

    fun String.toInstantAtEpochMillis() = Instant.ofEpochMilli(toLong())

    data class IndexFile(val timestamp: Instant, val contentStream: Stream<Document>)
    data class Document(val fields: Map<String, FieldValue>)
    data class FieldValue(val flags: Set<Flag>, val value: String) {
        override fun toString() = value
    }
    enum class Flag {
        INDEXED, ANALYZED, STORED
    }
    data class Field(val flags: Set<Flag>, val name: String, val value: String)
}