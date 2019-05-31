package nz.ac.auckland.concert.client.service;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.*;
import nz.ac.auckland.concert.common.message.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import javax.imageio.ImageIO;
import javax.ws.rs.NotFoundException;

/**
 * AWS client that handles downloading images from an AWS S3 bucket.
 */
public class AWSClient {

    // AWS S3 access credentials for concert images.
    private static final String AWS_ACCESS_KEY_ID = "AKIAJOG7SJ36SFVZNJMQ";
    private static final String AWS_SECRET_ACCESS_KEY = "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";

    // Name of the S3 bucket that stores images.
    private static final String AWS_BUCKET = "concert2.aucklanduni.ac.nz";

    // Download directory - a directory named "images" in the user's home
    // directory.
    private static final String FILE_SEPARATOR = System
            .getProperty("file.separator");
    private static final String USER_DIRECTORY = System
            .getProperty("user.home");
    private static final String DOWNLOAD_DIRECTORY = USER_DIRECTORY
            + FILE_SEPARATOR + "images";

    private AmazonS3 _s3;

    private static Logger _logger = LoggerFactory.getLogger(AWSClient.class);

    public AWSClient() {

        // Create download directory if it doesn't already exist.
        File downloadDirectory = new File(DOWNLOAD_DIRECTORY);
        downloadDirectory.mkdir();

        // Create an AmazonS3 object that represents a connection with the
        // remote S3 service.
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(
                AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY);
        _s3 = AmazonS3ClientBuilder
                .standard()
                .withRegion(Regions.AP_SOUTHEAST_2)
                .withCredentials(
                        new AWSStaticCredentialsProvider(awsCredentials))
                .build();
    }

    /**
     * Downloads a specified image from an AWS bucket and returns it as an AWT image
     * @param imageName name of image to download
     * @return the image
     */
    public Image retrieveImage(String imageName) {
        File imageFile = downloadImage(imageName);
        return readImage(imageFile);
    }

    /**
     * Downloads an image from the bucket named AWS_BUCKET.
     *
     * @param imageName the name of the image to download
     * @return the file the image has downloaded to
     */
    private File downloadImage(String imageName) {

        File downloadDirectory = new File(DOWNLOAD_DIRECTORY);

        File imageFile = new File(downloadDirectory, imageName);

        if (!imageFile.exists()) {
            GetObjectRequest req = new GetObjectRequest(AWS_BUCKET, imageName);

            try {
                _s3.getObject(req, imageFile);

            } catch (AmazonServiceException e) {
                throw new NotFoundException(Messages.NO_IMAGE_FOR_PERFORMER);
            }
        }

        return imageFile;
    }

    /**
     * Reads in a downloaded image from a file
     * @param imageFile to read from
     * @return the image
     */
    private Image readImage(File imageFile) {
        Image image = null;
        try {
            image = ImageIO.read(imageFile);
        } catch (IOException e) {
        }
        return image;
    }
}

