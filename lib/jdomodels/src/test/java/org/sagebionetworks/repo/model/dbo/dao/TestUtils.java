package org.sagebionetworks.repo.model.dbo.dao;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.sagebionetworks.repo.model.annotation.Annotations;
import org.sagebionetworks.repo.model.annotation.DateAnnotation;
import org.sagebionetworks.repo.model.annotation.DoubleAnnotation;
import org.sagebionetworks.repo.model.annotation.LongAnnotation;
import org.sagebionetworks.repo.model.annotation.StringAnnotation;
import org.sagebionetworks.repo.model.file.ExternalFileHandle;
import org.sagebionetworks.repo.model.file.PreviewFileHandle;
import org.sagebionetworks.repo.model.file.S3FileHandle;

import com.amazonaws.util.BinaryUtils;

public class TestUtils {

	/**
	 * Helper to create a S3FileHandle
	 * 
	 * @return
	 */
	public static S3FileHandle createS3FileHandle(String createdById) {
		return createS3FileHandle(createdById, 123);
	}

	/**
	 * Helper to create a S3FileHandle
	 * 
	 * @return
	 */
	public static S3FileHandle createS3FileHandle(String createdById, int sizeInBytes) {
		return createS3FileHandle(createdById, sizeInBytes, "content type");
	}

	/**
	 * Helper to create a S3FileHandle
	 * 
	 * @return
	 */
	public static S3FileHandle createS3FileHandle(String createdById, int sizeInBytes, String contentType) {
		S3FileHandle meta = new S3FileHandle();
		meta.setBucketName("bucketName");
		meta.setKey("key");
		meta.setContentType(contentType);
		meta.setContentSize((long)sizeInBytes);
		meta.setContentMd5("md5");
		meta.setCreatedBy(createdById);
		meta.setFileName("foobar.txt");
		return meta;
	}

	/**
	 * Helper to create a PreviewFileHandle
	 * @return
	 */
	public static PreviewFileHandle createPreviewFileHandle(String createdById) {
		return createPreviewFileHandle(createdById, 123);
	}

	/**
	 * Helper to create a PreviewFileHandle
	 * @return
	 */
	public static PreviewFileHandle createPreviewFileHandle(String createdById, int sizeInBytes) {
		return createPreviewFileHandle(createdById, 123, "content type");
	}

	/**
	 * Helper to create a PreviewFileHandle
	 * @return
	 */
	public static PreviewFileHandle createPreviewFileHandle(String createdById, int sizeInBytes, String contentType) {
		PreviewFileHandle meta = new PreviewFileHandle();
		meta.setBucketName("bucketName");
		meta.setKey("key");
		meta.setContentType(contentType);
		meta.setContentSize((long)sizeInBytes);
		meta.setContentMd5("md5");
		meta.setCreatedBy(createdById);
		meta.setFileName("preview.jpg");
		return meta;
	}

	/**
	 * Helper to create a PreviewFileHandle
	 * @return
	 */
	public static ExternalFileHandle createExternalFileHandle(String createdById) {
		return createExternalFileHandle(createdById, "content type");
	}

	/**
	 * Helper to create a PreviewFileHandle
	 * @return
	 */
	public static ExternalFileHandle createExternalFileHandle(String createdById, String contentType) {
		ExternalFileHandle meta = new ExternalFileHandle();
		meta.setExternalURL("http://www.example.com/");
		meta.setContentType(contentType);
		meta.setCreatedBy(createdById);
		meta.setFileName("External");
		return meta;
	}

	/**
	 * Calculate the MD5 digest of a given string.
	 * @param tocalculate
	 * @return
	 */
	public static String calculateMD5(String tocalculate){
		try {
			MessageDigest digetst = MessageDigest.getInstance("MD5");
			byte[] bytes = digetst.digest(tocalculate.getBytes("UTF-8"));
			return  BinaryUtils.toHex(bytes);	
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Create a populated Annotations object.
	 * 
	 * @return
	 */
	public static Annotations createDummyAnnotations() {
		
		List<StringAnnotation> stringAnnos = new ArrayList<StringAnnotation>();
		StringAnnotation sa = new StringAnnotation();
		sa.setIsPrivate(false);
		sa.setKey("sa");
		sa.setValue("foo");
		stringAnnos.add(sa);
		
		List<LongAnnotation> longAnnos = new ArrayList<LongAnnotation>();
		LongAnnotation la = new LongAnnotation();
		la.setIsPrivate(true);
		la.setKey("la");
		la.setValue(42L);
		longAnnos.add(la);
		
		List<DoubleAnnotation> doubleAnnos = new ArrayList<DoubleAnnotation>();
		DoubleAnnotation doa = new DoubleAnnotation();
		doa.setIsPrivate(false);
		doa.setKey("doa");
		doa.setValue(3.14);
		doubleAnnos.add(doa);
		
		Annotations annos = new Annotations();
		annos.setStringAnnos(stringAnnos);
		annos.setLongAnnos(longAnnos);
		annos.setDoubleAnnos(doubleAnnos);
		return annos;
	}
}
