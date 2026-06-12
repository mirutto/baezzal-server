package server.image

import com.drew.imaging.ImageMetadataReader
import com.drew.imaging.ImageProcessingException
import com.drew.metadata.Metadata
import com.drew.metadata.bmp.BmpHeaderDirectory
import com.drew.metadata.exif.ExifDirectoryBase
import com.drew.metadata.exif.ExifIFD0Directory
import com.drew.metadata.exif.ExifSubIFDDirectory
import com.drew.metadata.file.FileTypeDirectory
import com.drew.metadata.gif.GifHeaderDirectory
import com.drew.metadata.jpeg.JpegDirectory
import com.drew.metadata.png.PngDirectory
import com.drew.metadata.webp.WebpDirectory
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import java.io.IOException

@Component
class ImageMetadataExtractor {
    fun extract(bytes: ByteArray): ImageMetadata {
        if (bytes.isEmpty()) {
            throw ImagePlatformException("이미지 바이트 배열이 비어 있습니다")
        }

        val metadata =
            try {
                ImageMetadataReader.readMetadata(ByteArrayInputStream(bytes))
            } catch (exception: ImageProcessingException) {
                throw ImagePlatformException("이미지 메타데이터를 추출할 수 없습니다", exception)
            } catch (exception: IOException) {
                throw ImagePlatformException("이미지 메타데이터를 읽는 중 오류가 발생했습니다", exception)
            }

        val fileTypeDirectory =
            metadata.getFirstDirectoryOfType(FileTypeDirectory::class.java)
                ?: throw ImagePlatformException("이미지 파일 타입을 판별할 수 없습니다")

        val width = extractWidth(metadata)
        val height = extractHeight(metadata)
        val mimeType =
            fileTypeDirectory.getString(FileTypeDirectory.TAG_DETECTED_FILE_MIME_TYPE)
                ?: throw ImagePlatformException("이미지 MIME 타입을 판별할 수 없습니다")
        val fileExtension =
            fileTypeDirectory.getString(FileTypeDirectory.TAG_EXPECTED_FILE_NAME_EXTENSION)
                ?.lowercase()
                ?: throw ImagePlatformException("이미지 확장자를 판별할 수 없습니다")
        val orientation =
            metadata.getFirstDirectoryOfType(ExifIFD0Directory::class.java)
                ?.getInteger(ExifDirectoryBase.TAG_ORIENTATION)

        return ImageMetadata(
            width = width,
            height = height,
            aspectRatio =  width.toDouble() / height.toDouble(),
            mimeType = mimeType,
            fileExtension = fileExtension,
            orientation = orientation,
        )
    }

    private fun extractWidth(metadata: Metadata): Int =
        metadata.getFirstDirectoryOfType(ExifSubIFDDirectory::class.java)
            ?.getInteger(ExifDirectoryBase.TAG_EXIF_IMAGE_WIDTH)
            ?: metadata.getFirstDirectoryOfType(JpegDirectory::class.java)
                ?.getInteger(JpegDirectory.TAG_IMAGE_WIDTH)
            ?: metadata.getFirstDirectoryOfType(PngDirectory::class.java)
                ?.getInteger(PngDirectory.TAG_IMAGE_WIDTH)
            ?: metadata.getFirstDirectoryOfType(GifHeaderDirectory::class.java)
                ?.getInteger(GifHeaderDirectory.TAG_IMAGE_WIDTH)
            ?: metadata.getFirstDirectoryOfType(WebpDirectory::class.java)
                ?.getInteger(WebpDirectory.TAG_IMAGE_WIDTH)
            ?: metadata.getFirstDirectoryOfType(BmpHeaderDirectory::class.java)
                ?.getInteger(BmpHeaderDirectory.TAG_IMAGE_WIDTH)
            ?: throw ImagePlatformException("이미지 너비를 추출할 수 없습니다")

    private fun extractHeight(metadata: Metadata): Int =
        metadata.getFirstDirectoryOfType(ExifSubIFDDirectory::class.java)
            ?.getInteger(ExifDirectoryBase.TAG_EXIF_IMAGE_HEIGHT)
            ?: metadata.getFirstDirectoryOfType(JpegDirectory::class.java)
                ?.getInteger(JpegDirectory.TAG_IMAGE_HEIGHT)
            ?: metadata.getFirstDirectoryOfType(PngDirectory::class.java)
                ?.getInteger(PngDirectory.TAG_IMAGE_HEIGHT)
            ?: metadata.getFirstDirectoryOfType(GifHeaderDirectory::class.java)
                ?.getInteger(GifHeaderDirectory.TAG_IMAGE_HEIGHT)
            ?: metadata.getFirstDirectoryOfType(WebpDirectory::class.java)
                ?.getInteger(WebpDirectory.TAG_IMAGE_HEIGHT)
            ?: metadata.getFirstDirectoryOfType(BmpHeaderDirectory::class.java)
                ?.getInteger(BmpHeaderDirectory.TAG_IMAGE_HEIGHT)
            ?: throw ImagePlatformException("이미지 높이를 추출할 수 없습니다")
}
