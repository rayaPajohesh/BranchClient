package com.iac.branchClient.service.printer;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.List;
import javax.imageio.ImageIO;

import com.iac.branchClient.Updater;
import com.mca.MCALibTools;
import com.mca.MCASmart;

public class Mabna {
	private MCASmart mca = null;
	private Graphics2D g;
	private BufferedImage bufferedImage;
	private static final String errorInternalPrinterBeforeMagnet = "5555";
	private static final String errorInternalPrinterAfterMagnet = "6666";
//	private final String errorInternalApplet = "9999";
//	private final String errorInternalPrinter = "8888";
//	private final String errorStopStepCard = "7777";
	private final String generalError = "\u062e\u0637\u0627\u064a\u064a \u062f\u0631 \u0631\u0648\u0646\u062f \u0686\u0627\u067e \u06a9\u0627\u0631\u062a \u0631\u062e \u062f\u0627\u062f\u0647 \u0627\u0633\u062a.";
	private String printerKey;
	private RequestPrintDetail requestPrintDetail;
	private final Locale locale = new Locale("fa", "IR");
	boolean isTracksWritten = false;

	private final String SYSTEM_PATH = "C:\\BranchClient\\printer\\";

	public final Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	private final void writeObject(ObjectOutputStream out) throws IOException {
		throw new IOException("Object cannot be serialized");
	}

	private final void readObject(ObjectInputStream in) throws IOException {
		throw new IOException("Class cannot be Deserialized");
	}

	private String initMabna() {
		String result = "";
		Updater.info("Mabna", "initMabna()  <ENTER>");
		try {

			MCALibTools.checkInstall(SYSTEM_PATH + "MCASmart.jar", SYSTEM_PATH);

		} catch (Exception e) {
			result = generalError;//errorStopStepCard;
			Updater.error("Mabna", e);
			return result;
		}
		Updater.info("Mabna", "initMabna()  <EXIT>");
		return result;
	}

	public String Process(RequestPrintDetail val) {
		requestPrintDetail = val;
		isTracksWritten = false;

		String result = "";

		result = initMabna();
		if (result!=null&&!result.equals(""))
			return addCodeToResult(result);
		result = checkPrinter();
		if (result!=null&&!result.equals(""))
			return addCodeToResult(result);

		result = unlockPrinter(requestPrintDetail.getPrinterKey());
		if (result!=null&&!result.equals(""))
			return addCodeToResult(result);
		result = cardInPrinter();
		if (result!=null&&!result.equals(""))
			return addCodeToResult(result);

		result = MakeBitmap(requestPrintDetail.getCardObjectDetail());
		if (result!=null&&!result.equals(""))
			return addCodeToResult(result);

		/*
		 * SaveBitmapToFile("e:\\image.bmp"); result = "7777" + ";" +
		 * "Save Imgae to e:\\image.bmp";
		 */

		result = printCard();
		if (result!=null&&!result.equals(""))
			return addCodeToResult(result);
		result = cardOut();
		if (result!=null&&!result.equals(""))
			return addCodeToResult(result);
		result = lockPrinter();
		if (result!=null&&!result.equals(""))
			return addCodeToResult(result);
		result = releaseInstance();
		if (result!=null&&!result.equals(""))
			return addCodeToResult(result);
		return result;
	}

	private String addCodeToResult(String result) {
		if(isTracksWritten){
			result = errorInternalPrinterAfterMagnet + ";" + result;
		}else /*if(!result.equals(errorInternalApplet)&&!result.equals(errorInternalPrinter))*/{
				result = errorInternalPrinterBeforeMagnet + ";" + result;
		}
		return result;
	}

	/*
	 * private void SaveBitmapToFile(String filePath) { try {
	 * 
	 * ImageIO.write(bufferedImage, "BMP", new File(filePath)); } catch
	 * (IOException e) { // TODO Auto-generated catch block //
	 * e.printStackTrace(); } }
	 */

	private String checkPrinter() {
		String result = "";
		Updater.info("Mabna", "checkPrinter()  <ENTER>");
		try {
			ArrayList<String> listPrinter = new ArrayList<String>();
			int resultListPrinter = MCASmart.GetDeviceList(listPrinter);
			if (resultListPrinter != 0) {
				result = getErrorCodeDesc(resultListPrinter);

				Updater.info("Mabna", "checkPrinter() " + "Error=" + result + "  <EXIT>");
				return result;

			} else
				mca = MCASmart.getInstance(listPrinter.get(0));
		} catch (Exception e) {
			result = generalError;//errorStopStepCard;
			Updater.error("Mabna", e);
			return result;

		}
		Updater.info("Mabna", "checkPrinter()  <EXIT>");
		return result;
	}

	private String unlockPrinter(String val) {
		String result = "";
		Updater.info("Mabna", "unlockPrinter()  <ENTER>");
		try {

			int resultUNLock = mca.Unlock(val, 0);
			if (resultUNLock != 0) {
				result = getErrorCodeDesc(resultUNLock);
				releaseInstance();
				Updater.info("Mabna", "unlockPrinter() " + "Error=" + result + "  <EXIT>");
				return result;
			}

			printerKey = val;
		} catch (Exception e) {
			result = generalError;//errorStopStepCard;
			Updater.error("Mabna", e);
			releaseInstance();
			return result;
		}
		Updater.info("Mabna", "unlockPrinter() " + " <EXIT>");
		return result;
	}

	private String cardInPrinter() {
		String result = "";
		Updater.info("Mabna", "cardInPrinter()  <ENTER>");
		try {

			int resultCardIn = mca.CardIn();
			if (resultCardIn != 0) {
				result = getErrorCodeDesc(resultCardIn);
				Updater.info("Mabna", "cardInPrinter() " + "Error=" + result + "  <EXIT>");
				lockPrinter();
				releaseInstance();
				return result; 

			}

		} catch (Exception e) {
			result = generalError;//errorStopStepCard;
			Updater.error("Mabna", e);
			lockPrinter();
			releaseInstance();
			return result;
		}

		Updater.info("Mabna","cardInPrinter() " + " <EXIT>");
		return result;
	}

	private String MakeBitmap(List<CardObjectDetail> cardObjectDetail) {
		String result = "";
		Updater.info("Mabna", "MakeBitmap()  <ENTER>");
		try {
			IndexColorModel icm = new IndexColorModel(8, 2, new byte[] {
					(byte) 0, (byte) 0xFF },
					new byte[] { (byte) 0, (byte) 0xFF }, new byte[] {
							(byte) 0, (byte) 0xFF });
			bufferedImage = new BufferedImage(1016, 648,
					BufferedImage.TYPE_BYTE_BINARY, icm);

			g = bufferedImage.createGraphics();

			g.setColor(Color.white);
			g.fillRect(0, 0, 1016, 648);
			g.setColor(Color.BLACK);
			int intColNumber = 0;
			int intRowNumber = 0;
			for (CardObjectDetail list : cardObjectDetail) {
				String fieldValue = list.getFieldValue().trim();
				intColNumber = list.getColNum();
				intRowNumber = list.getRowNum();

				Font objFont = new Font(list.getFontName(), Font.BOLD,
						list.getFontSize() * 120 / 300)
						.deriveFont((float) (list.getFontSize() * 120 / 300.));
				g.setFont(objFont);
				MakeImageRow(list.getFieldName(), fieldValue, objFont,
						intRowNumber, intColNumber, "");

			}
			g.dispose();

		} catch (Exception e) {
			result = generalError;//errorInternalApplet;
			Updater.error("Mabna", e);
			cardOut();
			lockPrinter();
			releaseInstance();
		}
		Updater.info("Mabna", "MakeBitmap() "
				+ " <EXIT>");
		return result;
	}

	private void MakeImageRow(String strFieldName, String strFieldValue,
			Font oFont, int intRow, int intColumn, String StrPicturePath)
			throws Exception {
		float sngPosition = 0;

		Updater.info("Mabna", "MakeImageRow()  <ENTER>");

		sngPosition = (g.getFontMetrics().getHeight()) * intRow * 120 / 300;
		int padding;
		int actual_width = g.getFontMetrics().stringWidth(strFieldValue);
		padding = intColumn * g.getFontMetrics().getHeight() - actual_width
				- 300;
		if (strFieldName.equalsIgnoreCase("CARDTEXT")) {
			if (strFieldValue != null) {

				g.drawString(strFieldValue, padding - 200, sngPosition);
			}
		} else if (strFieldName.equalsIgnoreCase("CARDPICTURE")
				&& !isEmpty(strFieldValue)) {

			BufferedImage img = ImageIO.read(new File(strFieldValue));

			g.drawImage(img, intColumn, intRow, img.getWidth(),
					img.getHeight(), null);
		} else {
			if (strFieldName.equalsIgnoreCase("Customer_Full_Name")) {

				g.drawString(strFieldValue, padding, sngPosition);
			} else
				g.drawString(strFieldValue, intColumn
						* g.getFontMetrics().getHeight() * 120 / 300,
						sngPosition);

		}
		Updater.info("Mabna", "MakeImageRow()  <EXIT>");

	}

	private String printCard() {
		String result = "";
		Updater.info("Mabna", "printCard()  <ENTER>");
		try {
			int resultWriteTrack = mca.WriteMS(
					requestPrintDetail.getTrack_1_Data(),
					requestPrintDetail.getTrack_2_Data(),
					requestPrintDetail.getTrack_3_Data());

			if (resultWriteTrack != 0) {
				result = getErrorCodeDesc(resultWriteTrack);
				Updater.info("Mabna", "printCard() " + "Error=" + result + "  <EXIT>");
				cardOut();
				lockPrinter();
				releaseInstance();
				return result;
			} else {
				isTracksWritten = true;
				File file = new File(SYSTEM_PATH + "varnish.bmp");
				int resultPrintImage;
				if (!file.exists())
					resultPrintImage = mca.PrintImage(null, bufferedImage,
							null, null);
				else {
					BufferedImage varnishBuffer = ImageIO.read(file);
					resultPrintImage = mca.PrintImage(null, bufferedImage,
							null, varnishBuffer);
				}
				if (resultPrintImage != 0) {
					result = getErrorCodeDesc(resultPrintImage);
					Updater.info("Mabna", "printCard() " + "Error=" + result + "  <EXIT>");
					cardOut();
					lockPrinter();
					releaseInstance();
					return result;
				}
			}

		} catch (Exception e) {
			result = generalError;//errorInternalPrinter;
			Updater.error("Mabna", e);
			cardOut();
			lockPrinter();
			releaseInstance();
		}
		Updater.info("Mabna", "printCard() "
				+ " <EXIT>");
		return result;
	}

	private String cardOut() {
		String result = "";
		Updater.info("Mabna", "cardOut()  <ENTER>");
		try {
			int resultCardOut = mca.CardOut();
			if (resultCardOut != 0) {
				result = getErrorCodeDesc(resultCardOut);
				Updater.info("Mabna", "cardOut() " + "Error=" + result + " <EXIT>");
				lockPrinter();
				releaseInstance();
				return result;
			}

		} catch (Exception e) {
			result = generalError;//errorInternalPrinter;
			Updater.error("Mabna", e);
			lockPrinter();
			releaseInstance();
		}
		Updater.info("Mabna", "cardOut() "
				+ " <EXIT>");
		return result;
	}

	private String lockPrinter() {
		String result = "";
		Updater.info("Mabna", "lockPrinter()  <ENTER>");
		try {
			int resultLockPrinter = mca.Lock(printerKey);
			if (resultLockPrinter != 0) {
				result = getErrorCodeDesc(resultLockPrinter);
				Updater.info("Mabna", "lockPrinter() " + "Error=" + result + " <EXIT>");
				releaseInstance();
				return result;

			}

		} catch (Exception e) {
			result = generalError;//errorInternalPrinter;
			Updater.error("Mabna", e);
			releaseInstance();
		}
		Updater.info("Mabna", "lockPrinter() "
				+ " <EXIT>");
		return result;
	}

	private String releaseInstance() {
		String result = "";
		Updater.info("Mabna", "releaseInstance()  <ENTER>");
		try {
			int resultRealese = mca.ReleaseInstance();
			if (resultRealese != 0) {
				result = getErrorCodeDesc(resultRealese);
				Updater.info("Mabna", "releaseInstance() " + "Error=" + result + " <EXIT>");
				mca = null;
				return result;
			}
			mca = null;
		} catch (Exception e) {
			result = generalError;//errorInternalPrinter;
			Updater.error("Mabna", e);
		}
		Updater.info("Mabna", "releaseInstance() " + " <EXIT>");
		return result;
	}

	private boolean isEmpty(Object str) {

		if (str == null || ((String) str).length() == 0) {
			return true;

		}
		if ((String) str.toString().trim() == null) {

			return true;

		}

		else {

			return false;
		}

	}

	@SuppressWarnings("static-access")
	private String getErrorCodeDesc(int errorCode) {
		String result = "";
		if (errorCode == -1)
			return generalError;//errorInternalPrinter;
		String desc = mca.GetErrorMessage(errorCode, locale);
		if (desc.startsWith("Unknown error code"))
			return generalError;//errorInternalPrinter;
		result = desc + ":" +errorCode;
		result.replaceAll("\"", "");
		return result;
	}
}
