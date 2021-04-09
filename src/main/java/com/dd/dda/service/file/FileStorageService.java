package com.dd.dda.service.file;

import com.dd.dda.config.FileStorageConfiguration;
import com.dd.dda.model.FileType;
import com.dd.dda.model.Rawfile;
import com.dd.dda.model.exception.DDAException;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FileStorageService {

    private static final String FAILED_TO_STORE_FILE = "Failed to store file! ";


    @Autowired
    private FileStorageConfiguration storageConfiguration;



    public void moveFile(Path orginalFile, Path targetDirectory) {
        if (orginalFile == null || targetDirectory == null) {
            throw new DDAException(String.format("orignal File parameter \"%s\" or new directory \"%s\" are empty", orginalFile, targetDirectory));
        }
        File file = orginalFile.toFile();
        if(!file.exists() || !file.isFile()) {
            throw new DDAException(String.format("orignal File doesn't exist or is not a file  \"%s\"" , orginalFile.toString()));
        }

        try {
            if (!targetDirectory.getParent().toFile().exists()) {
                Files.createDirectories(targetDirectory.getParent());
            }
            Files.move(orginalFile, targetDirectory, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Error during moving the file \"{}\" to directory \"{}\"", orginalFile, targetDirectory, e);
            throw new DDAException(String.format("Error during moving the file \"%s\" to directory \"%s\"", orginalFile, targetDirectory));
        }
    }

    public void deleteFile(Path xmlFile) {
        try {
            FileSystemUtils.deleteRecursively(xmlFile);
        } catch (IOException e) {
            throw new DDAException("Could not delete file for " + xmlFile , e);
        }
    }

    private Path store(Path targetPath, byte[] bytes, String fileName) {
        try {
            if(targetPath == null) {
                throw new DDAException("Failed due to target path empty value! " + getAttachmentInfo(targetPath, fileName));
            }

            if (StringUtils.isEmpty(fileName)) {
                throw new DDAException("Failed due to file with empty name! " + getAttachmentInfo(targetPath, fileName));
            }
            if (bytes == null || bytes.length == 0) {
                throw new DDAException("Failed to store empty file! " + getAttachmentInfo(targetPath, fileName));
            }
            return storeAttachment(new ByteArrayInputStream(bytes), targetPath);
        } catch (IOException e) {
            throw new DDAException(FAILED_TO_STORE_FILE + getAttachmentInfo(targetPath, fileName));
        }
    }





    private Path storeAttachment(InputStream inputStream, Path path) throws IOException {
        Files.createDirectories(path.getParent());
        Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
        return path;
    }



    private String getAttachmentInfo(Path targetPath, String fileName) {
        return "targetPath: " + targetPath + ", fileName = " + fileName;
    }

    ////////////////////////////

    public void delete(String path) {
        try {
            if (!FileSystemUtils.deleteRecursively(Path.of(path))) {
                throw new DDAException("File does not exist! " + path);
            }
        } catch (IOException e) {
            throw new DDAException("Could not delete file for " + path);
        }
    }


    private Path fileSpecificPath(FileType fileType, Long id) {
        if (fileType == FileType.USERVERIFICATION) {
            return Path.of(storageConfiguration.getVerificationProofLocation().toString(), "" + id);
        }
        if (fileType == FileType.USERPROFILEPIC) {
            return Path.of(storageConfiguration.getProfilcePicLocation().toString(),""+id);
        }
        if (fileType == FileType.PARLIAMENTFLAG) {
            return Path.of(storageConfiguration.getParliamentPicLocation().toString(),""+id);
        }
        if (fileType == FileType.BILLABSTRACT) {
            return Path.of(storageConfiguration.getBillAbstractLocation().toString(),""+id);
        }
        if (fileType == FileType.BILLFILES) {
            return Path.of(storageConfiguration.getBillFilesLocation().toString(),""+id);
        }
        throw new DDAException("Filetype unknown");
    }

    private Path fileSpecificPath(FileType fileType, Long id, MultipartFile file) {
        if (fileType == FileType.USERVERIFICATION) {
            return Path.of(storageConfiguration.getVerificationProofLocation().toString(), "" + id, file.getOriginalFilename());
        }
        if (fileType == FileType.USERPROFILEPIC) {
            return Path.of(storageConfiguration.getProfilcePicLocation().toString(),""+id, file.getOriginalFilename());
        }
        if (fileType == FileType.PARLIAMENTFLAG) {
            return Path.of(storageConfiguration.getParliamentPicLocation().toString(),""+id, file.getOriginalFilename());
        }
        if (fileType == FileType.BILLABSTRACT) {
            return Path.of(storageConfiguration.getBillAbstractLocation().toString(),""+id, file.getOriginalFilename());
        }
        if (fileType == FileType.BILLFILES) {
            return Path.of(storageConfiguration.getBillFilesLocation().toString(),""+id, file.getOriginalFilename());
        }
        throw new DDAException("Filetype unknown");
    }

    private Path compressedProfilePicPath(Long id){
        return Path.of(storageConfiguration.getProfilcePicLocation().toString(),""+id, "pr0f1lepIc.jpg");
    }

    private Path resizedProfilePicPath(Long id){
        return Path.of(storageConfiguration.getProfilcePicLocation().toString(),""+id, "pr0f1lepIcResized.png");
    }


    public String storeFile(FileType fileType, Long id, MultipartFile file) throws IOException {
        Path path = fileSpecificPath(fileType, id, file);
        Files.createDirectories(path.getParent());
        if(fileType == FileType.USERPROFILEPIC){
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            try (InputStream is = new FileInputStream(path.toFile())) {
                resize(is, path, IMG_WIDTH, IMG_HEIGHT);
            }
            if(isJPG(file)) {
                compress(path.toString(), path.toString(), file.getSize());
            } else {
                compress(path.toString(), newJpgPath(path), file.getSize());
                delete(path.toString());
            }
            //
            //delete(resizedProfilePicPath(id).toString());

        } else {

            if( isPicture(file) && file.getSize() > 200000){

                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);


                try (InputStream is = new FileInputStream(path.toFile())) {
                    resize(is, path);
                }
                if( isJPG(file)) {
                    compress(path.toString(), path.toString(), file.getSize());
                    //delete(path.toString());
                } else {
                    compress(path.toString(), newJpgPath(path), file.getSize());
                    delete(path.toString());
                }
                //delete(resizedProfilePicPath(id).toString());

            } else {
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        return path.toString();
    }

    public boolean isPicture(MultipartFile file){
        return file.getOriginalFilename().toLowerCase().endsWith(".png") || file.getOriginalFilename().toLowerCase().endsWith(".jpg");
    }

    public boolean isJPG(MultipartFile file){
        return file.getOriginalFilename().toLowerCase().endsWith(".jpg");
    }

    public String newJpgPath(Path p){
        String s = p.toString();
        int i = s.lastIndexOf(".");
        return s.substring(0,i) + ".jpg";
    }

    private void compress(String fromPath, String toPath, long size) throws IOException {


        File input = new File(fromPath);
        BufferedImage image = ImageIO.read(input);

        File compressedImageFile = new File(toPath);
        OutputStream os = new FileOutputStream(compressedImageFile);

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        ImageWriter writer = (ImageWriter) writers.next();

        ImageOutputStream ios = ImageIO.createImageOutputStream(os);
        writer.setOutput(ios);

        ImageWriteParam param = writer.getDefaultWriteParam();



        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(0.5f);  // Change the quality value you prefer
        writer.write(null, new IIOImage(image, null, null), param);

        os.close();
        ios.close();
        writer.dispose();

    }

    private final int IMG_WIDTH = 128;
    private final int IMG_HEIGHT = 128;

    private void resize(InputStream input, Path target,
                               int width, int height) throws IOException {
        // read an image to BufferedImage for processing
        BufferedImage originalImage = ImageIO.read(input);
        // create a new BufferedImage for drawing
        BufferedImage newResizedImage
                = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = newResizedImage.createGraphics();
        g.setComposite(AlphaComposite.Src);
        g.fillRect(0, 0, width, height);
        Map<RenderingHints.Key,Object> hints = new HashMap<>();
        hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.addRenderingHints(hints);

        // puts the original image into the newResizedImage
        int ow = originalImage.getWidth();
        int oh = originalImage.getHeight();
        int nw, nh, x, y;
        if(oh >= ow){
            nw = width;
            nh = height * oh / ow;
            x = 0;
            y = -(oh - ow) * height / oh / 2;
        } else {
            nh = height;
            nw = width * ow / oh;
            y = 0;
            x = -(ow - oh) * width / ow / 2;
        }

        g.drawImage(originalImage, x, y, nw, nh, null);
        g.dispose();

        // get file extension
        String s = target.getFileName().toString();
        String fileExtension = s.substring(s.lastIndexOf(".") + 1);

        // we want image in png format
        ImageIO.write(newResizedImage, fileExtension, target.toFile());

    }

    private void resize(InputStream input, Path target) throws IOException {
        // read an image to BufferedImage for processing
        BufferedImage originalImage = ImageIO.read(input);

        int ow = originalImage.getWidth();
        int oh = originalImage.getHeight();

        final int pixelcnt = 1024*1024;
        final double q = 1.0 * ow / oh;
        int nw = (int) Math.sqrt(pixelcnt * q);
        int nh = pixelcnt / nw;

        BufferedImage newResizedImage
                = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = newResizedImage.createGraphics();
        g.setComposite(AlphaComposite.Src);
        g.fillRect(0, 0, nw, nh);
        Map<RenderingHints.Key,Object> hints = new HashMap<>();
        hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.addRenderingHints(hints);

        // puts the original image into the newResizedImage

        g.drawImage(originalImage, 0, 0, nw, nh, null);
        g.dispose();

        // get file extension
        String s = target.getFileName().toString();
        String fileExtension = s.substring(s.lastIndexOf(".") + 1);

        // we want image in png format
        ImageIO.write(newResizedImage, fileExtension, target.toFile());

    }




    public List<Rawfile> getFiles(FileType fileType, Long id){
        Path path = fileSpecificPath(fileType, id);
        List<Rawfile> result2;
        try {
            result2 = Files.list(path).map(p -> {
                try {
                    byte[] bytes = Files.readAllBytes(p);
                    String n = p.getFileName().toString();
                    return new Rawfile(bytes, n);


                } catch (IOException e) {
                    e.printStackTrace();
                    throw new DDAException("IOException beim Lesen des VerificationProof-Ordner von " + id );
                }
            }).collect(Collectors.toList());
        } catch (IOException e) {
            return new ArrayList<>();
            //e.printStackTrace();
            //throw new DDAException("IOException beim Lesen des VerificationProof-Ordner von " + id );
        }
        return result2;
    }




    public void deleteAllFiles(FileType fileType, Long id) {

        Path path = fileSpecificPath(fileType, id);

        if(Files.exists(path)){
            try {
                Files.list(path).forEach(p -> delete(p.toString()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    public boolean filesExist(FileType fileType, Long id){
        Path path = fileSpecificPath(fileType, id);

        try {
            return !Files.list(path).findAny().isEmpty();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;

    }

    public List<Rawfile> getFilesBundle(FileType fileType, List<Long> ids) {
        List<Rawfile> result = new ArrayList<>();
        for(Long id : ids) {
            Path path = fileSpecificPath(fileType, id);
            List<Rawfile> result2;
            try {
                result2 = Files.list(path).map(p -> {
                    try {
                        byte[] bytes = Files.readAllBytes(p);
                        String n = p.getFileName().toString();
                        return new Rawfile(bytes, n);
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new DDAException("IOException beim Lesen des VerificationProof-Ordner von " + id);
                    }
                }).collect(Collectors.toList());
                if(result2.isEmpty()){
                    result.add(null);
                } else {
                    result.add(result2.get(0));
                }
            } catch (IOException e) {
                result.add(null);
            }
        }
        return result;
    }





    public void downloadFile(FileType fileType, Long id, String url) throws IOException {
        String path = fileSpecificPath(fileType, id).toString();
        Connection.Response res = Jsoup.connect(url)
                .userAgent("Mozilla")
                .timeout(30000)
                .followRedirects(true)
                .ignoreContentType(true)
                .maxBodySize(20000000)//Increase value if download is more than 20MB
                .execute();
        String remoteFilename=res.header("Content-Disposition").replaceFirst("(?i)^.*filename=\"?([^\"]+)\"?.*$", "$1");
        String filename = Path.of( path , remoteFilename).toString();
        FileOutputStream out = (new FileOutputStream(new java.io.File(filename)));
        out.write( res.bodyAsBytes());
        out.close();
    }




}
