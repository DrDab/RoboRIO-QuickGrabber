import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.Scanner;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

public class GrabberRunner 
{
	public static int TEAM_NUMBER = -1;
	public static String ROBORIO_IP = "";
	public static Scanner inputRead;
	static String HOME_DIR_DESKTOP = System.getProperty("user.home")+"/Desktop/";
	
	public static void main (String[] args) throws IOException
	{
		loadTeamNum();
		inputRead = new Scanner(System.in);
		if (TEAM_NUMBER == -1)
		{
			System.out.println("                  =====[Quick RoboRIO Log Fetcher]=====");
			promptTeamNum();
			writeTeamNum(TEAM_NUMBER);
		}
		ROBORIO_IP = "ftp://roboRIO-" + TEAM_NUMBER + "-frc.local";
		drawBanner();
		System.out.println();
		boolean isquit = false;
		while (!isquit)
		{
			int choice = -1;
			System.out.println("[1] Fetch logs from " + ROBORIO_IP);
			System.out.println("[2] Fetch logs from custom IP");
			System.out.println("[3] Set Team#");
			System.out.println("[0] Quit");
			System.out.print("CHOICE> ");
			String parse = inputRead.nextLine();
			System.out.println();
			try
			{
				choice = Integer.parseInt(parse);
			}
			catch (NumberFormatException e)
			{
				System.err.println("\n\nERROR: Please enter a valid number. Your input: " + parse);
			}
			if (choice == 1)
			{
				fetchLogs(ROBORIO_IP);
			}
			else if (choice == 2)
			{
				System.out.println("Enter custom IP: ");
				String tempIP = inputRead.nextLine();
				fetchLogs(tempIP);
			}
			else if (choice == 3)
			{
				promptTeamNum();
			}
			else if (choice == 0)
			{
				isquit = !isquit;
			}
		}
	}
	
	public static boolean fetchLogs(String IPAddr) throws IOException
	{
		try
		{
			FTPClient ftpClient = new FTPClient();
			ftpClient.connect(IPAddr, 21);
			ftpClient.login("anonymous", "");
			FTPFile[] subFiles = ftpClient.listFiles("/home/lvuser/tracelog/");
			if (subFiles != null && subFiles.length > 0) 
			{
				for (int i = 0; i < subFiles.length; i++) 
				{
					String currentFileName = subFiles[i].getName();
					if (currentFileName.equals(".") || currentFileName.equals("..")) 
					{
						// skip parent directory and the directory itself
						continue;
					}
					String filePath = "/home/lvuser/tracelog/" + currentFileName;
	                boolean success = downloadSingleFile(ftpClient, filePath, HOME_DIR_DESKTOP + "RoboRIO-Files/");
	                if (success) 
	                {
	                    System.out.println("[OK] " + filePath);
	                    return true;
	                } 
	                else
	                {
	                    System.out.println("[FAIL] " + filePath);
	                    return false;
	                }
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}
	
	public static boolean downloadSingleFile(FTPClient ftpClient, String remoteFilePath, String savePath) throws IOException 
	{
	    File downloadFile = new File(savePath);
	     
	    File parentDir = downloadFile.getParentFile();
	    if (!parentDir.exists()) 
	    {
	        parentDir.mkdir();
	    }
	         
	    OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile));
	    try 
	    {
	        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
	        return ftpClient.retrieveFile(remoteFilePath, outputStream);
	    } 
	    catch (IOException ex)
	    {
	        throw ex;
	    } 
	    finally 
	    {
	        if (outputStream != null)
	        {
	            outputStream.close();
	        }
	    }
	}
	
	public static void writeTeamNum(int num) throws IOException
	{
		File f = new File(HOME_DIR_DESKTOP + "RoboRIO-Files/");
		if (!f.exists())
		{
			f.mkdir();
		}
		File read = new File(f, "teamnum.cfg");
		if (!read.exists()) 
		{
			read.createNewFile();
		}
		PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(read)));
		pw.println(num);
		pw.flush();
		pw.close();
	}
	
	public static void loadTeamNum() throws IOException
	{
		File f = new File(HOME_DIR_DESKTOP + "RoboRIO-Files/");
		if (!f.exists())
		{
			f.mkdir();
		}
		File read = new File(f, "teamnum.cfg");
		if (!read.exists()) 
		{
			read.createNewFile();
		}
		BufferedReader br = new BufferedReader(new FileReader(read));
		try
		{
			TEAM_NUMBER = Integer.parseInt(br.readLine());
		}
		catch (Exception e)
		{
			TEAM_NUMBER = -1;
		}
		br.close();
	}
	
	public static void promptTeamNum()
	{
		boolean pass = false;
		System.out.println("Please enter your team number.");
		while (!pass) 
		{
			System.out.print("TEAM #> ");
			String parse = inputRead.nextLine();
			try
			{
				TEAM_NUMBER = Integer.parseInt(parse);
				pass = !pass;
			}
			catch (NumberFormatException e)
			{
				System.err.println("\n\nERROR: Please enter a valid number. Your input: " + parse);
			}
		}
	}
	
	public static void drawBanner()
	{
		System.out.println("                  =====[Quick RoboRIO Log Fetcher]=====");
		System.out.print("                       [ Team Number : " + TEAM_NUMBER);
		for(int i = 0; i < 25 - (15 + (int) (Math.log10(TEAM_NUMBER) + 1)); i++)
		{
			System.out.print(" ");
		}
		System.out.print("]");
	}
	
}
