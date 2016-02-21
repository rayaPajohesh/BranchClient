

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.imageio.ImageIO;

import com.iac.branchClient.Updater;
import com.iac.branchClient.service.printer.CardObjectDetail;
import com.iac.branchClient.service.printer.RequestPrintDetail;

public class Evolis {
	private static final String errorInternalPrinterBeforeMagnet = "5555";
	private static final String errorInternalPrinterAfterMagnet = "6666";
	private static final String printerHome = "C:\\BranchClient\\printer\\";
	private static final String EVOLIS_DEFAULT_PRINTER_NAME = "Evolis Pebble";
	private static boolean isExceptionUnkown = false;
	
	static public boolean isInit = false;

	static {
		if(!isInit){
			try {
				System.load(printerHome + "iomemJNI.dll");
				System.load(printerHome + "prn_adapter_2.0.dll");
		          isInit = true;
		      } catch (UnsatisfiedLinkError ex) {
		    	  isInit = false;
		    	  Updater.error("Evolis", ex);
		      }
		}
	}

	public static String Process(RequestPrintDetail requestPrintDetail) {
		String result = "";
		String printerName = "";
		printerName = EVOLIS_DEFAULT_PRINTER_NAME;
		boolean isTracksWritten = false;
		try {
			preparePrinter(printerName, requestPrintDetail.getPrinterKey());
			writeTracks(printerName, result, requestPrintDetail.getTrack_1_Data(), requestPrintDetail.getTrack_2_Data());
			isTracksWritten = true;
			prepareToPrint(printerName);
			printCard(requestPrintDetail, printerName);
			printOverlay(printerName);
			releasPrinter(printerName, requestPrintDetail.getPrinterKey());
		} catch (Exception e) {
			Updater.error("Evolis", e);
			result = translateMessage(e.getMessage()) + ":" + e.getMessage();
			/*if(e.getMessage().equals("PRINTER NOT FOUND")){
				result = errorPrinterNotFound + ";" + result;
			}else */if(isTracksWritten){
				result = errorInternalPrinterAfterMagnet + ";" + result;
			}else if(!isExceptionUnkown){
				result = errorInternalPrinterBeforeMagnet + ";" + result;
			}/*else{
				result = errorInternalPrinter + ";" + result;
			}*/
			try{
				releasPrinter(printerName, requestPrintDetail.getPrinterKey());
			}catch (Error er) {
				Updater.error("Evolis", er);
			}catch (Exception ex) {
				Updater.error("Evolis", ex);
			}
		}
		return result;
	}
	
	private static String translateMessage(String message){
		isExceptionUnkown = false;
		String translatedMessage = "";
		if(message.equals("DLL ERROR")){
			translatedMessage = "\u0645\u0634\u06a9\u0644\u064a \u062f\u0631 \u0641\u0627\u064a\u0644 \u0647\u0627\u064a \u062f\u0631\u0627\u064a\u0648\u0631 \u0645\u0648\u0631\u062f \u0646\u064a\u0627\u0632 \u0686\u0627\u067e\u06af\u0631 \u0648\u062c\u0648\u062f \u062f\u0627\u0631\u062f.";
		}else if(message.equals("ERR")
				||message.equals("ERR OPEN")
				||message.equals("ERR CLOSE")){
			translatedMessage="\u0627\u0631\u062a\u0628\u0627\u0637 \u0628\u0627 \u0686\u0627\u067e\u06af\u0631 \u0628\u0631\u0642\u0631\u0627\u0631 \u0646\u0645\u064a \u0628\u0627\u0634\u062f.";
			translatedMessage += "\u06a9\u0646\u062a\u0631\u0644 \u0646\u0645\u0627\u064a\u064a\u062f \u0686\u0627\u067e\u06af\u0631 \u0628\u0631\u0631\u0648\u064a \u0633\u064a\u0633\u062a\u0645 \u0646\u0635\u0628 \u0634\u062f\u0647 \u0628\u0627\u0634\u062f.";
			translatedMessage += "\u0627\u062a\u0635\u0627\u0644 \u06a9\u0627\u0628\u0644 \u0686\u0627\u067e\u06af\u0631 \u0628\u0647 \u06a9\u0627\u0645\u067e\u064a\u0648\u062a\u0631 \u0628\u0631\u0642\u0631\u0627\u0631 \u0628\u0627\u0634\u062f.";
			translatedMessage += "\u0627\u062a\u0635\u0627\u0644 \u0628\u0631\u0642 \u0686\u0627\u067e\u06af\u0631 \u0628\u0631\u0642\u0631\u0627\u0631 \u0628\u0627\u0634\u062f.";
			
		}else if(message.equals("ERR PARAMETRES")
				||message.equals("ERROR PARAMETRES")){
			translatedMessage = "\u0645\u062c\u0648\u0632 \u0627\u0633\u062a\u0641\u0627\u062f\u0647 \u0627\u0632 \u0686\u0627\u067e\u06af\u0631 \u06a9\u0627\u0631\u062a \u0645\u0648\u062c\u0648\u062f \u0646\u0645\u064a \u0628\u0627\u0634\u062f.";
		}else if(message.equals("FEEDER EMPTY")){
			translatedMessage = "\u0645\u062e\u0632\u0646 \u06a9\u0627\u0631\u062a \u0686\u0627\u067e\u06af\u0631 \u062e\u0627\u0644\u064a \u0645\u064a \u0628\u0627\u0634\u062f\u060c \u064a\u06a9 \u06a9\u0627\u0631\u062a \u062e\u0627\u0645 \u062f\u0631 \u0686\u0627\u067e\u06af\u0631 \u0642\u0631\u0627\u0631 \u062f\u0647\u064a\u062f.";
		}else if(message.equals("ERR RIBBON")
				||message.equals("ERR RIBBON TYPE")
				||message.equals("ERROR RIBBON")){
			translatedMessage = "\u062e\u0637\u0627\u064a \u0631\u064a\u0628\u0648\u0646 \u0686\u0627\u067e\u06af\u0631\u060c \u0641\u0642\u062f\u0627\u0646 \u0631\u064a\u0628\u0648\u0646\u060c \u0627\u062a\u0645\u0627\u0645 \u0631\u064a\u0628\u0648\u0646\u060c \u0628\u0631\u064a\u062f\u0647 \u0634\u062f\u0646 \u0631\u064a\u0628\u0648\u0646 \u0648 \u064a\u0627 \u0639\u062f\u0645 \u0627\u0646\u062a\u062e\u0627\u0628 \u0635\u062d\u064a\u062d \u0646\u0648\u0639 \u0631\u064a\u0628\u0648\u0646 \u0631\u0627 \u06a9\u0646\u062a\u0631\u0644 \u0646\u0645\u0627\u064a\u064a\u062f.";
		}else if(message.equals("ERROR MAGN")
				||message.equals("ERR READ")
				||message.equals("ERR WRITE")){
			translatedMessage = "\u0639\u0645\u0644\u064a\u0627\u062a \u0646\u0648\u0634\u062a\u0646 \u0628\u0631\u0631\u0648\u064a \u0634\u064a\u0627\u0631 \u0645\u063a\u0646\u0627\u0637\u064a\u0633\u064a \u06a9\u0627\u0631\u062a \u0628\u0627 \u0645\u0648\u0641\u0642\u064a\u062a \u0628\u0647 \u0627\u0646\u062c\u0627\u0645 \u0646\u0631\u0633\u064a\u062f. \u0627\u0632 \u0635\u062d\u062a \u06a9\u0627\u0631\u062a \u0648 \u062c\u0647\u062a \u0642\u0631\u0627\u0631 \u06af\u0631\u0641\u062a\u0646 \u0622\u0646 \u062f\u0631 \u0686\u0627\u067e\u06af\u0631 \u0627\u0637\u0645\u064a\u0646\u0627\u0646 \u062d\u0627\u0635\u0644 \u0646\u0645\u0627\u064a\u064a\u062f.";
		}else if(message.equals("ERROR TIME OUT")
				||message.equals("ERR TIME")
				||message.equals("Time out")){
			translatedMessage = "\u067e\u0627\u0633\u062e\u064a \u0627\u0632 \u0686\u0627\u067e\u06af\u0631 \u06a9\u0627\u0631\u062a \u062f\u0631\u064a\u0627\u0641\u062a \u0646\u0634\u062f\u060c \u0686\u0627\u067e\u06af\u0631 \u0631\u0627 \u0631\u064a\u0633\u062a \u0646\u0645\u0627\u064a\u064a\u062f.";
		}else if(message.equals("COVER OPEN")){
			translatedMessage = "\u062f\u0631\u067e\u0648\u0634 \u0686\u0627\u067e\u06af\u0631 \u0628\u0627\u0632 \u0627\u0633\u062a.";
		}else if(message.equals("BMP ERROR")){
			translatedMessage = "\u0645\u0634\u06a9\u0644\u064a \u062f\u0631 \u0631\u0648\u0646\u062f \u0633\u0627\u062e\u062a\u0646 \u062a\u0635\u0648\u064a\u0631 \u0631\u0648\u064a \u06a9\u0627\u0631\u062a \u0631\u062e \u062f\u0627\u062f\u0647 \u0627\u0633\u062a.";
		}else if(message.equals("PRINTER NOT FOUND")){
			translatedMessage = "\u0686\u0627\u067e\u06af\u0631\u064a \u064a\u0627\u0641\u062a \u0646\u0634\u062f.";
		}
		else{
			isExceptionUnkown = true;
			translatedMessage = "\u062e\u0637\u0627\u064a\u064a \u062f\u0631 \u0631\u0648\u0646\u062f \u0686\u0627\u067e \u06a9\u0627\u0631\u062a \u0631\u062e \u062f\u0627\u062f\u0647 \u0627\u0633\u062a.";
		}
		return translatedMessage;
	}

	private static void releasPrinter(String printerName, String printerKey) throws Exception {
		String result;
		Updater.info("Evolis", "releasPrinter  <ENTER>");
		result = IomemJNI.sendCommandUSB(printerName, "Se");
		if(!result.equalsIgnoreCase("ok")){
			result = IomemJNI.sendCommandUSB(printerName, "Se");
			if(!result.equalsIgnoreCase("ok")){
				throw new Exception(result);
			}
		}
		result = IomemJNI.sendCommandUSB(printerName, "Pkey;D;" + printerKey);
		if(!result.equalsIgnoreCase("ok")){
			throw new Exception(result);
		}
		Updater.info("Evolis", "releasPrinter  <EXIT>");
	}

	private static void printOverlay(String printerName) throws IOException, Exception {
		String result;
		String ss;
		byte[] pp;
		byte[] ssb;
		Updater.info("Evolis", "printOverlay  <ENTER>");
		byte[] varnish = fileToByteArray(new File(printerHome	+ "varnish.bmp").getAbsoluteFile());
		ss = "Dbmp;o;0;648;0;";
		pp = new byte[varnish.length + ss.length() + 1];
		ssb = ss.getBytes();
		for (int i = 0; i < ssb.length; i++) {
			pp[i] = ssb[i];
		}
		for (int j = ssb.length; j < varnish.length + ssb.length; j++) {
			pp[j] = varnish[j - ssb.length];
		}
		result = IomemJNI.sendBinariesUSB(printerName, pp);
		if(!result.equalsIgnoreCase("ok")){
			throw new Exception(result);
		}
		Updater.info("Evolis", "printOverlay  <EXIT>");
	}

	private static void printCard(RequestPrintDetail requestPrintDetail, String printerName) throws Exception, IOException {
		String result;
		Updater.info("Evolis", "printCard  <ENTER>");
		BufferedImage bufferedImage = MakeBitmap(requestPrintDetail.getCardObjectDetail());
		byte[] imageBytes = bufferedImageToByteArray(bufferedImage);
		String ss = "Dbmp;k;0;648;0;";
		byte[] pp = new byte[imageBytes.length + ss.length() + 1];
		byte[] ssb = ss.getBytes();
		for (int i = 0; i < ssb.length; i++) {
			pp[i] = ssb[i];
		}
		for (int j = ssb.length; j < imageBytes.length + ssb.length; j++) {
			pp[j] = imageBytes[j - ssb.length];
		}
		result = IomemJNI.sendBinariesUSB(printerName, pp);
		if(!result.equalsIgnoreCase("ok")){
			throw new Exception(result);
		}
		Updater.info("Evolis", "printCard  <EXIT>");
	}

	private static void prepareToPrint(String printerName) throws Exception {
		String result;
		Updater.info("Evolis", "prepareToPrint  <ENTER>");
		result = IomemJNI.sendCommandUSB(printerName, "Wcb;k");
		if(!result.equalsIgnoreCase("ok")){
			throw new Exception(result);
		}
		result = IomemJNI.sendCommandUSB(printerName, "Wcb;o");
		if(!result.equalsIgnoreCase("ok")){
			throw new Exception(result);
		}
		result = IomemJNI.sendCommandUSB(printerName, "Pc;k;=;10");
		if(!result.equalsIgnoreCase("ok")){
			throw new Exception(result);
		}
		Updater.info("Evolis", "prepareToPrint  <EXIT>");
	}

	private static void preparePrinter(String printerName, String printerKey) throws Exception {
		String result = "";
		Updater.info("Evolis", "prepareEvolis  <ENTER>");
		if(!isInit){
			result = "DLL ERROR";
			throw new Exception(result);
		}else{
			result = "OK";
		}
		
		result = IomemJNI.sendCommandUSB(printerName, "Pkey;E;" + printerKey);
		if(!result.equalsIgnoreCase("ok")){
			throw new Exception(result);
		}
		result = IomemJNI.sendCommandUSB(printerName, "Pem;2");
		if(!result.equalsIgnoreCase("ok")){
			throw new Exception(result);
		}
		result = IomemJNI.sendCommandUSB(printerName, "Si");
		if(!result.equalsIgnoreCase("ok")){
			throw new Exception(result);
		}
		result = IomemJNI.sendCommandUSB(printerName, "Pr;ko");
		if(!result.equalsIgnoreCase("ok")){
			throw new Exception(result);
		}
		result = IomemJNI.sendCommandUSB(printerName, "Ss");
		if(!result.equalsIgnoreCase("ok")){
			throw new Exception(result);
		}
		Updater.info("Evolis", "prepareEvolis  <EXIT>");
	}

	private static void writeTracks(String printerName, String result, String track1, String track2) throws Exception {
		Updater.info("Evolis", "writeTracks  <ENTER>");
		result = IomemJNI.sendCommandUSB(printerName, "Dm;1;" + track1);
		if(!result.equalsIgnoreCase("ok")){
			throw new Exception(result);
		}
		result = IomemJNI.sendCommandUSB(printerName, "Dm;2;" + track2);
		if(!result.equalsIgnoreCase("ok")){
			throw new Exception(result);
		}
		result = IomemJNI.sendCommandUSB(printerName, "Smw");
		if(!result.equalsIgnoreCase("ok")){
			throw new Exception(result);
		}
		Updater.info("Evolis", "writeTracks  <EXIT>");
	}
	
	private static BufferedImage MakeBitmap(List<CardObjectDetail> cardObjectDetail) throws Exception {
		Updater.info("Evolis", "MakeBitmap()  <ENTER>");
		BufferedImage bufferedImage = null;
		Graphics2D g;
		try {
			IndexColorModel icm = new IndexColorModel(8, 2, new byte[] {	(byte) 0, (byte) 0xFF },
					new byte[] { (byte) 0, (byte) 0xFF }, new byte[] {
							(byte) 0, (byte) 0xFF });
			bufferedImage = new BufferedImage(1016, 648,	BufferedImage.TYPE_BYTE_BINARY, icm);
			g = bufferedImage.createGraphics();
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, 1016, 648);
			g.setColor(Color.white);
			int intColNumber = 0;
			int intRowNumber = 0;
			for (CardObjectDetail list : cardObjectDetail) {
				String fieldValue = list.getFieldValue().trim();
				intColNumber = list.getColNum();
				intRowNumber = list.getRowNum();
				Font objFont = new Font(list.getFontName(), Font.BOLD, list.getFontSize() * 120 / 300)
						.deriveFont((float) (list.getFontSize() * 120 / 300.));
				g.setFont(objFont);
				MakeImageRow(g, list.getFieldName(), fieldValue, objFont, intRowNumber, intColNumber, "");
			}
			g.dispose();
		} catch (Exception e) {
			Updater.error("Evolis", e);
			throw new Exception("BMP ERROR");
		}
		Updater.info("Evolis", "MakeBitmap() "	+ " <EXIT>");
		return bufferedImage;
	}

	private static void MakeImageRow(Graphics2D g, String strFieldName, String strFieldValue,	Font oFont
			, int intRow, int intColumn, String StrPicturePath) throws Exception {
		float sngPosition = 0;
		Updater.info("Evolis", "MakeImageRow()  <ENTER>");
		sngPosition = (g.getFontMetrics().getHeight()) * intRow * 120 / 300;
		int padding;
		int actual_width = g.getFontMetrics().stringWidth(strFieldValue);
		padding = intColumn * g.getFontMetrics().getHeight() - actual_width	- 300;
		if (strFieldName.equalsIgnoreCase("CARDTEXT")) {
			if (strFieldValue != null) {
				g.drawString(strFieldValue, padding - 200, sngPosition);
			}
		} else if (strFieldName.equalsIgnoreCase("CARDPICTURE") && !isEmpty(strFieldValue)) {
			BufferedImage img = ImageIO.read(new File(strFieldValue));
			g.drawImage(img, intColumn, intRow, img.getWidth(), img.getHeight(), null);
		} else {
			if (strFieldName.equalsIgnoreCase("Customer_Full_Name")) {
				g.drawString(strFieldValue, padding, sngPosition);
			} else{
				g.drawString(strFieldValue, intColumn	*g.getFontMetrics().getHeight()*120 / 300, sngPosition);
			}
		}
		Updater.info("Evolis", "MakeImageRow()  <EXIT>");
	}
	
	@SuppressWarnings("resource")
	public static byte[] fileToByteArray(File file) throws IOException {
		InputStream is = new FileInputStream(file);
		long length = file.length();
		byte[] bytes = new byte[(int) length];
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length	&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}
		if (offset < bytes.length) {
			throw new IOException("Could not completely read file " + file.getName());
		}
		is.close();
		return bytes;
	}
	
    public static byte[] bufferedImageToByteArray(BufferedImage bufferedImage) {
        byte[] imb = null;
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, "bmp", b);
            b.flush();
            imb = b.toByteArray();
            b.close();
        } catch (IOException ex) {
        	Updater.error("Evolis", ex);
        }
        return imb;
    }
	
	private static boolean isEmpty(Object str) {
		if (str == null || ((String) str).length() == 0) {
			return true;
		}
		if ((String) str.toString().trim() == null) {
			return true;
		} else {
			return false;
		}
	}
	
	public static void main(String[] args) {
		
	}
}
