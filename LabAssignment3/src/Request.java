import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;



public class Request implements Runnable{
	
	String requestContent=null;
	String tmpStrLocation="firstTry";
	
	String[] primaryCommand=null;
	String outputPrimaryCommand="";
	int svrPortNumber=0;
	
	public Request(String[] primaryCommand) {
		
		this.primaryCommand=new String[primaryCommand.length];
		for(int i=0;i<primaryCommand.length;i++) {
			
				this.primaryCommand[i]=primaryCommand[i];
			
			
		}
		
	}
	
	public Request(int svrPortNumber) {
		this.svrPortNumber=svrPortNumber;
	}
	
	
	private String[] clearEmptyElements(String[] cmdLineArr){
		String[] tmp=new String[cmdLineArr.length];
		int j=0;
		for(int i=0;i<cmdLineArr.length;i++){
			if(!cmdLineArr[i].equalsIgnoreCase("")){
				tmp[j]=cmdLineArr[i];
				j++;
			}
		}
		return tmp;
	}
	
	public String createRequest(String[] cmdLineArr) throws MalformedURLException {	
		
		String postHeaderOptions="";
		String postBodyOptions="";
		String result="";
		String cmdLineString;
		
		
		while(cmdLineArr.length>1){
			
			    cmdLineString=createCmdLineString(cmdLineArr);
			    
			    cmdLineArr=clearEmptyElements(cmdLineArr);
			    //********
				CommandLineParser clp=new CommandLineParser(cmdLineArr);
				int outValue=clp.validateCommandLine(clp.cmdLineArray);

				
				
				switch (outValue){
					case 0:

						break;
						
					case ErrorDescriptions.HTTPC_ERROR_CODE:
						return ErrorDescriptions.HTTPC_ERROR_DSC;
						
					case -2:
						return "Error -2";
		
						
					case -3:
						return "Error -3";
		
						
					case -4:
						return "Error -4";
					case -100:
						return "Error -100";
					
					case -6000:
						return "Error -6000";
		
						
				}
				Object monitor=new Object();
				//------------------------------------------------------------------------		
				if(clp.getMainCmd().equalsIgnoreCase("GET")) {
					
					requestContent=clp.getMainCmd().toUpperCase() + " " +
								   clp.getPath();
					Thread threadSender;
					Thread threadReceiver;
					//writetofile
					Utility.writeToFile(Constants.currentPath+"/test/client/out/command.txt",requestContent);
					
					String[] args=new String[]{"3000","8000","8007",Constants.currentPath+"/test/client/out/command.txt","command.txt"};
					
					Sender s= new Sender(Integer.parseInt(args[0]), Integer.parseInt(args[1]),Integer.parseInt(args[2]) ,args[3], args[4]);
					threadSender=new Thread(s,"threadSenderInRequest");
					threadSender.start();
					
					args=new String[]{"9000","3000","9007",Constants.currentPath+"/test/client/in"};
					Receiver r= new Receiver(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]),args[3],0,monitor);
					threadReceiver=new Thread(r,"threadReceiverInRequest");
					threadReceiver.start();
					
					synchronized (monitor) {
						try {
							monitor.wait();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
				    System.out.println(result);
				    
				}
				//------------------------------------------------------------------------		
				if(clp.getMainCmd().equalsIgnoreCase("POST")) {
					postHeaderOptions=clp.getMainCmd().toUpperCase() + " " +
								  clp.getPath();
					
					if(clp.getParamD()!=null) {
						if(clp.getParamD().length>0) {
							for(String str:clp.getParamD()) {
								postBodyOptions=postBodyOptions+ str + "\n";
							}	
							postBodyOptions=postBodyOptions.substring(0, postBodyOptions.length()-1);
							
						}
					}
					
					//TODO
					if(clp.getParamF()!=null) {
						if(clp.getParamF().length>0) {
							postBodyOptions = readFile(clp.getParamF()[0]);
						}
					}
					
					
					if(clp.getParamH()!=null) {
					if(clp.getParamH().length>0) {
						for(String str:clp.getParamH()) {
							postHeaderOptions=postHeaderOptions+ str + "\n";
						}
						
					}
					}
					

					requestContent=postHeaderOptions + " " + postBodyOptions;
					
					
					//writetofile
					
					Utility.writeToFile(Constants.currentPath+"/test/client/out/command.txt",requestContent);
					

					
					String[] args=new String[]{"3000","8000","8007",Constants.currentPath+"/test/client/out/command.txt","command.txt"};
					Sender s= new Sender(Integer.parseInt(args[0]), Integer.parseInt(args[1]),Integer.parseInt(args[2]) ,args[3], args[4]);
					Thread t=new Thread(s);
					t.start();
					
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
								
					args=new String[]{"9000","3000","9007", Constants.currentPath+"/test/client/in"};
					
					Receiver r= new Receiver(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]),args[3],0,monitor);
					Thread t1=new Thread(r);
					t1.start();
					
					synchronized (monitor) {
						try {
							monitor.wait();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

					result=Utility.fileReader(Constants.currentPath+"/test/client/in/result.txt");

				    System.out.println(result);
				}
				
				//------------------------------------------------------------------------
				if(clp.getMainCmd().equalsIgnoreCase("HELP")) {
					if(clp.getHelpCommand().equalsIgnoreCase("GET")) {
						System.out.println("usage: httpc get [-v] [-h key:value] URL");
						return "No Error";
					}
					
					if(clp.getHelpCommand().equalsIgnoreCase("POST")) {
						System.out.println("usage: httpc post [-v] [-h key:value] [-d inline-data] [-f file] URL");
						return "No Error";
					}
				}
				
				requestContent="";
				String tmpCmdLineRedirect="";
			    
			    
			    //----------------
				tmpStrLocation=processResult(result);
				if(tmpStrLocation!=null ){
					cmdLineString =redirect(tmpStrLocation, cmdLineString, clp.getUrlFirstIndex(), clp.getUrlLastIndex(),clp.getPath().length());
					
				}else{
					cmdLineString="";
				}
				cmdLineArr=cmdLineString.trim().split(" ");	

		}
		
		
		return "No Error";
	}
	
	public void writeFile(String fileName,String str) {
		PrintWriter out;
		try {
			out = new PrintWriter(fileName, "UTF-8");
			out.write(str);
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
            
	}
	
	public String readFile(String filePath) {
		Scanner in;
		String output="";
		try {
			in = new Scanner(new FileReader(filePath));
		
		StringBuilder sb = new StringBuilder();
		while(in.hasNext()) {
		    sb.append(in.next());
		}
		in.close();
		output= sb.toString();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return output;
	}
	
	private String processResult(String result){
		int index=result.indexOf("Location: ");
		int tmpIndex=0;
		String tmpStr="";
		if(index>0){
			tmpStr=result.substring(index+9);
			tmpIndex=tmpStr.indexOf("\n");
			return tmpStr.substring(0, tmpIndex);
		}
		return null;
	}
	
	private String redirect(String location,String cmdLine,int urlFirstIndex,int urlLastIndex, int lenPath) throws MalformedURLException{
		String tmpPartOne="";
		String tmpPartTwo="";
		String tmpCmdLine="";
		URL tmpurl = new URL(cmdLine.substring(urlFirstIndex,urlLastIndex+lenPath));		
		cmdLine=cmdLine.replaceAll(tmpurl.getPath().trim(), location.trim());
	
		return cmdLine;
	}
	
	private String createCmdLineString(String[] cmdLineArr){
		
		String cmdLineString="";
		
		for(String tmpStr:cmdLineArr){
			cmdLineString=cmdLineString+" "+tmpStr;
		}
		
		return cmdLineString;
			 
		
	}
	
	
	public static void main(String[] args) {
		
		String output="";
		StringBuilder sbText=new StringBuilder();
		

//		*********************GET ListOfFile Test*******************
		Request rqst=new Request("GET http://localhost/".split(" "));
		Thread rqstThread=new Thread(rqst);				
		rqstThread.start();
		System.out.println(rqst.outputPrimaryCommand);		
//		*********************END*******************
		
		
//		*********************GET ContentOfFile Test*******************
//		Request rqst=new Request(("GET http://localhost/form01.pdf/" ).split(" "));
//		Thread rqstThread=new Thread(rqst);				
//		rqstThread.start();
//		System.out.println(rqst.outputPrimaryCommand);		
//		*********************END*******************		

//		*********************GET ContentOfFile Test*******************
//		Request rqst=new Request(("GET http://localhost/xmltest.xml/" ).split(" "));
//		Thread rqstThread=new Thread(rqst);				
//		rqstThread.start();
//		System.out.println(rqst.outputPrimaryCommand);		
//		*********************END*******************	
		
		
//		*********************Post test*******************	
//		StringBuilder sbText1=new StringBuilder();
//		sbText1.append("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
//		sbText1.append(System.lineSeparator());
//		sbText1.append("bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb");
//		sbText1.append(System.lineSeparator());
//		sbText1.append("cccccccccccccccccccccccccccccccccccccc");
//		sbText1.append(System.lineSeparator());
//		sbText1.append("dddddddddddddddddddddddddddddddddddddd");
//		sbText1.append(System.lineSeparator());
//		sbText1.append("eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
//		sbText1.append(System.lineSeparator());
//		sbText1.append("ffffffffffffffffffffffffffffffffffffff");
//		sbText1.append(System.lineSeparator());
//		sbText1.append("gggggggggggggggggggggggggggggggggggggg");
//		sbText1.append(System.lineSeparator());
//		sbText1.append("hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh");
//		sbText1.append(System.lineSeparator());
//		sbText1.append("iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii");
//		sbText1.append(System.lineSeparator());
//		sbText1.append("jjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjj");
//		sbText1.append(System.lineSeparator());
//		sbText1.append("kkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk");
//		sbText1.append(System.lineSeparator());
//		
//		Request rqst=new Request(("post --d " + sbText1.toString() + " http://localhost/second.txt").split(" "));
//		Thread rqstThread=new Thread(rqst);
//		rqstThread.start();
		
//		*********************END*******************			
		
		if (output!=""){
			System.out.println(output);
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			
			outputPrimaryCommand=createRequest(primaryCommand);
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
