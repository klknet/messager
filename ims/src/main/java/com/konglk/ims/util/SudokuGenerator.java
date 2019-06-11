package com.konglk.ims.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Created by konglk on 2019/6/8.
 * 生成九宫格头像
 * 群聊头像生成器
 */
@Component
public class SudokuGenerator {

    private Logger logger = LoggerFactory.getLogger(getClass());
    private int boardWidth = 112<<1;
    private int boardHeight = boardWidth;
    private int gap = 6;
    private int oneGapSize = boardHeight-gap*2;
    private int threeGapSize = (boardHeight-gap*3)/2;
    private int fiveGapSize = (boardHeight-gap*4)/3;

    public BufferedImage clipImages(String[] profileUrls) {
        BufferedImage mergedImage = new BufferedImage(boardWidth, boardHeight, BufferedImage.TYPE_INT_RGB);
        try {
            BufferedImage newImage = new BufferedImage(boardWidth, boardHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = newImage.createGraphics();
            graphics.setBackground(new Color(255, 255, 255));
            graphics.clearRect(0, 0, boardWidth, boardHeight);
            BufferedImage[] profileImgs = new BufferedImage[profileUrls.length];
            int length = profileUrls.length;
            int width = length == 1 ? oneGapSize : length<5? threeGapSize: fiveGapSize;
            for (int i=0; i<profileUrls.length; i++) {
                String profile = profileUrls[i];
                BufferedImage profileImage = changeSize(width, width, profile);
                profileImgs[i] = profileImage;
            }
            int size = profileImgs.length;
            for (int i=0; i<profileImgs.length; i++) {
                switch (size) {
                    case 1:
                        graphics.drawImage(profileImgs[i], gap, gap, null);
                        break;
                    case 2:
                        if (i == 0)
                            graphics.drawImage(profileImgs[i], gap, (boardHeight-threeGapSize)/2, null);
                        else
                            graphics.drawImage(profileImgs[i], gap*2+threeGapSize, (boardHeight-threeGapSize)/2, null);
                        break;
                    case 3:
                        if (i == 0)
                            graphics.drawImage(profileImgs[i], (boardHeight-threeGapSize)/2, gap, null);
                        else
                            graphics.drawImage(profileImgs[i], gap*i+(i-1)*threeGapSize, threeGapSize+gap*2, null);
                        break;
                    case 4:
                        graphics.drawImage(profileImgs[i], gap*(i%2+1)+(i%2)*threeGapSize, gap*(i/2+1)+threeGapSize*(i/2), null);
                        break;
                    case 5:
                        if (i<2) {
                            graphics.drawImage(profileImgs[i], (boardWidth-fiveGapSize*2)/2+i*(fiveGapSize+gap), (boardHeight-fiveGapSize*2)/2, null);
                        }else
                            graphics.drawImage(profileImgs[i], gap*((i+1)%3+1)+fiveGapSize*((i+1)%3), (boardHeight-fiveGapSize*2)/2+fiveGapSize+gap, null);
                        break;
                    case 6:
                        graphics.drawImage(profileImgs[i], gap*(i%3 + 1)+fiveGapSize*(i%3), (boardHeight-fiveGapSize*2)/2+fiveGapSize*(i/3)+gap*(i/3), null);
                        break;
                    case 7:
                        if (i==0)
                            graphics.drawImage(profileImgs[i], (boardWidth-fiveGapSize)/2, gap, null);
                        else
                            graphics.drawImage(profileImgs[i], gap*((i-1)%3+1)+fiveGapSize*((i-1)%3),
                                    gap*((i-1)/3+2)+fiveGapSize*((i-1)/3+1), null);
                        break;
                    case 8:
                        if (i<2)
                            graphics.drawImage(profileImgs[i], (boardWidth-fiveGapSize*2)/2+i*fiveGapSize+i*gap, gap, null);
                        else
                            graphics.drawImage(profileImgs[i], gap*((i-2)%3 + 1)+fiveGapSize*((i-2)%3),
                                    gap*((i-2)/3+2)+fiveGapSize*((i-2)/3+1), null);
                        break;
                    case 9:
                        graphics.drawImage(profileImgs[i], gap*(i%3+1)+fiveGapSize*(i%3), gap*(i/3+1)+fiveGapSize*(i/3), null);
                        break;
                }
            }
            return newImage;

        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return mergedImage;
    }

    /*
    改变图片size
     */
    private BufferedImage changeSize(int width, int height, String profile) throws IOException {
        BufferedImage bi;
        if(profile.startsWith("http")) {
            bi = ImageIO.read(new URL(profile));
        }else {
            bi = ImageIO.read(new File(profile));
        }
        Image itemp = bi.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        double ratio = 0; // 缩放比例
        if ((bi.getHeight() > height) || (bi.getWidth() > width)) {
            if (bi.getHeight() > bi.getWidth()) {
                ratio = (new Integer(height)).doubleValue()
                        / bi.getHeight();
            } else {
                ratio = (new Integer(width)).doubleValue() / bi.getWidth();
            }
            AffineTransformOp op = new AffineTransformOp(
                    AffineTransform.getScaleInstance(ratio, ratio), null);
            itemp = op.filter(bi, null);
        }
        BufferedImage image = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, width, height);
        if (width == itemp.getWidth(null)) {
            g.drawImage(itemp, 0, (height - itemp.getHeight(null)) / 2,
                    itemp.getWidth(null), itemp.getHeight(null),
                    Color.white, null);
        } else {
            g.drawImage(itemp, (width - itemp.getWidth(null)) / 2, 0,
                    itemp.getWidth(null), itemp.getHeight(null),
                    Color.white, null);
        }
        g.dispose();
        itemp = image;

        return (BufferedImage) itemp;
    }

    public static void main(String[] args) throws IOException {
        SudokuGenerator sg = new SudokuGenerator();
        String[] paths = {"http://39.106.133.40/static/maomao.png"};
        for(int i=1; i<=9; i++) {
            paths = new String[i];
            for (int j=i; j>0; j--) {
                paths[j-1] = "http://39.106.133.40/static/maomao.png";
            }
            BufferedImage image = sg.clipImages(paths);
            File file = new File("d:\\gp\\gpb"+i+".jpg");
            if(!file.exists())
                file.mkdirs();
            ImageIO.write(image, "JPG", file);
        }
//        int width = sg.threeGapSize;
//        BufferedImage image = sg.changeSize(width, width, "http://39.106.133.40/static/maomao.png");
//        File file = new File("d:\\gp\\rs"+width+".jpg");
//        if(!file.exists())
//            file.mkdirs();
//        ImageIO.write(image, "JPG", file);
//        sg.clipImages(paths);
    }


}
