package Classes;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Paint;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Stage;

/**
 *
 * @author Rafael
 */
public class TelaController implements Initializable {
    
    @FXML
    private TextField fileDir, outDir, namePattern, fpsInterval, newHeight, newWidth;
    @FXML
    private Button fileButton, outDirButton, initButton;//, closeButton, minButton;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private CheckBox rotate, redim;
    @FXML 
    private Spinner<Integer> rotateAngle;
    
    private VideoCapture video;
    private File outImagesDir;
    
    @FXML
    private void handleButtonPressed(MouseEvent event) {
        Button bt = (Button)event.getSource();
        bt.setTextFill(Paint.valueOf("#e6e6e6"));
//        if(event.getSource()==closeButton || event.getSource()==minButton){
//            bt.setStyle("-fx-background-color: #4f4f4f; -fx-border-color: #e6e6e6;");
//        }else{
            bt.setStyle("-fx-background-color: #4f4f4f; -fx-background-radius: 90; -fx-border-color: #e6e6e6; -fx-border-radius: 90;");
//        }
    }
    
    @FXML
    private void handleButtonRelease(MouseEvent event) {
        Button bt = (Button)event.getSource();
        bt.setTextFill(Paint.valueOf("#4f4f4f"));
        bt.setStyle("-fx-background-color: #e6e6e6; -fx-background-radius: 90; -fx-border-color: #4f4f4f; -fx-border-radius: 90;");
        if(event.getSource()==fileButton){
            String dir = fileChooser(1).getPath();
            video = new VideoCapture(dir);
            fileDir.setText(dir);
            newWidth.setText(Double.toString(video.get(3)));
            newHeight.setText(Double.toString(video.get(4)));
        }
        else if(event.getSource()==outDirButton){
            outImagesDir = fileChooser(0);
            outDir.setText(outImagesDir.getPath());
        }
//        else if(event.getSource()==closeButton){
//            bt.setStyle("-fx-background-color: #4a4a4a;");
//            System.exit(0);
//        }
//        else if(event.getSource()==minButton){
//            bt.setStyle("-fx-background-color: #4a4a4a;");
//            Stage s = (Stage) minButton.getScene().getWindow();
//            s.setIconified(true);
//        }
    }
    
    @FXML
    private void handleButtonClick(ActionEvent event) {
        if(event.getSource()==initButton){
            new Thread(new Runnable(){
                @Override
                public void run() {
                    JustDoIt();
                }
            }).start();
        }
    }
    
    @FXML
    private void handleTransform(ActionEvent event) {
        rotateAngle.setDisable(!rotate.isSelected());
        newHeight.setDisable(!redim.isSelected());
        newWidth.setDisable(!redim.isSelected());
    }
    
    private void JustDoIt(){
        int fInterval = Integer.parseInt(fpsInterval.getText());
        double totalFrames = video.get(7)/fInterval;
        Mat frame = new Mat();
        
        System.out.println("Altura: "+newHeight.getText()+ " largura: "+newWidth.getText());
        
        double new_heigth = Double.parseDouble(newHeight.getText());
        double new_width = Double.parseDouble(newWidth.getText());
        
        double angle = rotateAngle.getValue();
        
        if (!video.isOpened()) {
            System.out.println("Error! Video can't be opened!");
            return;
        }

        System.out.println("Total de frames estimado: "+totalFrames);
        
        int i = 0, c=0;

        while (video.read(frame)) {
            if(redim.isSelected()){
                Imgproc.resize(frame, frame, new Size(new_width, new_heigth), 0, 0, Imgproc.INTER_LINEAR);
            }
            if(rotate.isSelected()){
                Point center = new Point(frame.width() / 2, frame.height() / 2);
                Mat rotMatrix = Imgproc.getRotationMatrix2D(center, angle, 1.0); //1.0 means 100 % scale

                Imgproc.warpAffine(frame, frame, rotMatrix, frame.size());
            }
            
            Imgcodecs.imwrite(outImagesDir+"/"+namePattern.getText()+"_"+c+".jpg", frame);
            i+=fInterval;
            c++;
            video.set(1, i);
            
            System.out.println(c);
            frame.release();
            
            progressBar.setProgress(c/totalFrames);
        }
        video.release();
    }
    
    private File fileChooser(int type){
        if(type==0){
            DirectoryChooser fileChooser = new DirectoryChooser();
            fileChooser.setTitle("Selecione o diretório de saída");
            File selectedFile = fileChooser.showDialog(this.fileDir.getScene().getWindow());
            if (selectedFile != null) {
               return selectedFile;
            }
        }else if(type==1){
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Selecione o arquivo de vídeo");
            fileChooser.getExtensionFilters().addAll(
                    new ExtensionFilter("Video", "*.avi", "*.mp4"));
            File selectedFile = fileChooser.showOpenDialog(this.fileDir.getScene().getWindow());
            if (selectedFile != null) {
               return selectedFile;
            }
        }
        return null;
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
       SpinnerValueFactory<Integer> valueFactory = 
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 360, 180);
 
       rotateAngle.setValueFactory(valueFactory);
    }    
    
}
