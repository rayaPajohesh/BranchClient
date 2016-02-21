

import java.io.IOException;

import com.iac.branchClient.Updater;

public class IomemJNI {
	
	public static native int SetDebugLevel(int level);
	public static native int OpenPebble(String printerName);
	public static native boolean ClosePebble(int hPrn);
	public static native boolean WritePebble(int hPrn, String escCde, int szCde);
	public static native boolean WritebinPebble(int hPrn, byte[] data, int szCde);
	public static native String ReadPebble(int hPrn);
	
	public static String sendBinariesUSB(String printerName, byte[] content) throws IOException {
		String res = "";
		try {
			int cardPrinterId = 0;
			boolean bStatus = false;
			cardPrinterId = OpenPebble(printerName);
			if (cardPrinterId != 0) {
				bStatus = WritebinPebble(cardPrinterId, content, content.length);
				if (bStatus) {
					bStatus = false;
					res = ReadPebble(cardPrinterId);
				}
				ClosePebble(cardPrinterId);
			}
		} catch (Exception e) {
			res = e.getMessage();
			Updater.error("IomemJNI", e);
		}
		return res;
	}

	public static String sendCommandUSB(String printerName, String cmd) {
		String res = "";
		try {
			int cardPrinterId = 0;
			boolean bStatus = false;
			cardPrinterId = OpenPebble(printerName);
			if (cardPrinterId != 0) {
				String command = "\033" + cmd + "\015";
				bStatus = WritePebble(cardPrinterId, command, command.length());
				if (bStatus) {
					res = ReadPebble(cardPrinterId);
				}
				IomemJNI.ClosePebble(cardPrinterId);
			}else{
				res = "PRINTER NOT FOUND";
			}
		} catch (Exception e) {
			res = e.getMessage();
			Updater.error("IomemJNI", e);
		}
		return res;
	}
}
