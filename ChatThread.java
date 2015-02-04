import java.net.Socket;
import java.util.ArrayList;
import java.applet.Applet;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.URL;
import javax.imageio.*;
import java.applet.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;


/**
 * Created by WilliamRuppenthal on 1/15/15.
 * Upgraded by William Ruppenthal and Benjamin Most starting on 1/26/15
 * V1.2 1/27/15 7:10pm William implemented chatroom functionality
 */
public class ChatThread implements Runnable {
    protected Socket socket;
    public PrintWriter out;
    public BufferedReader in;
    public String name;
    public static ArrayList<ChatThread> threads;
    public ArrayList<String> blocked = new ArrayList<String>();
    public static ArrayList<Message> rooms;
    public String room;
    public String mostRecentChat;
    public String color="";
    public String color2="";
    public boolean foul=true, globalBlocked, muted;
    public ArrayList<String> shortcuts = new ArrayList<String>();
    public ArrayList<String> shortcuts2 = new ArrayList<String>();
	Sound sent, received, shrek;
    boolean active = true;

    public ChatThread(Socket socket) {
		try {
			sent = new Sound("sent.wav");
			received = new Sound("received.wav");
			shrek = new Sound("shrek.wav");
		}
		catch (Exception murle) {
			System.out.println(murle);
		}
		
        this.socket = socket;
		this.globalBlocked = false;
	
        if(ChatThread.threads==null)
            threads=new ArrayList<ChatThread>();

        if(ChatThread.rooms==null){
            rooms=new ArrayList<Message>();
            rooms.add(new Message("main",null));
        }

        this.room = "main";

        ChatThread.threads.add(this);

        try {
            this.out = new PrintWriter(this.socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

            //Get the user's name
            this.out.println("Please enter your username: ");
            String potentialName = this.in.readLine();
            for(int i = 0; i<threads.size(); i++) {
                if(potentialName.equals(threads.get(i).name)){
                    this.out.println("That name is taken, please choose another: ");
                    potentialName= this.in.readLine();
                    i=-1;
                }
            }
            this.name = potentialName;
            //Some debug
            System.out.println("Client " + this.name + " connected!");

            //Say hi to the client
            this.out.println("Welcome, " + this.name + ", to the chat server!\nType \\help to see the list of commands");
            this.out.println("Current users:");
            for(ChatThread c:ChatThread.threads)
                if (c.active)
                    this.out.println(c.name);

        } catch (IOException e) {
            System.out.println("IOException: " + e);
        }
    }

    public void run() {
        while (true) {
            try {
                //Get string from client
                String fromClient = this.in.readLine();
                //Commence horribly ugly if-else ladder to react to commands and
                //send the user's messages

                //If null, connection is closed, so just finish
                if (fromClient == null) {
                    active = false;
                    System.out.println("Client disconnected");
                    for(ChatThread c:ChatThread.threads)
                        if(c.room.equals(this.room))
                            c.out.println(this.name+" has disconnected.");
                    this.in.close();
                    this.out.close();
                    this.socket.close();
                    return;
                }
                else if (fromClient.indexOf("\\r ")>=0) {
                    fromClient = "\\whisper "+mostRecentChat+" " + fromClient.substring(fromClient.indexOf("\\r ")+3);
                }
                else if (fromClient.indexOf("\\defoul")>=0)
                    foul = false;
                else if (fromClient.indexOf("\\foul")>=0)
                    foul=true;
                else if (fromClient.indexOf("\\block")>=0 && fromClient.indexOf("blockglobal")<0) {
                    blocked.add(fromClient.substring(fromClient.indexOf("\\block")+7));
                }

                //If the client said "Exit chat", close the connection
                else if (fromClient.equalsIgnoreCase("Exit chat")) {
                    System.out.println("Client " + this.name + " said bye, disconnecting");
                    for(ChatThread c:ChatThread.threads)
                        c.out.println(this.name+" has disconnected.");
                    this.in.close();
                    this.out.close();
                    this.socket.close();
                    return;
                }

                else if(fromClient.equals("\\help")) {
                    this.out.println("\\whisper <name> <message> to send private message");
                    this.out.println("\\cr <name> (password) creates a new chatroom (password is optional)");
                    this.out.println("\\jr <name> (password) joins a chatroom");
                    this.out.println("\\lr lists all public rooms (default room is 'main')");
                    this.out.println("\\<color> changes color of your chat");
                    this.out.println("\\<color>2 changes color of incoming chat");
                    this.out.println("\\lc lists colors");
                    this.out.println("\\gr returns current room");
                    this.out.println("\\block <name> blocks user");
                    this.out.println("\\r replies to most recent whisper");
                    this.out.println("\\sc <text1> <text2> replaces every instance you type text1 with text2");
                    this.out.println("\\foul to allow foul language");
                    this.out.println("\\defoul to block foul language");
					this.out.println("\\mute to toggle mute/unmute");
					this.out.println("\\blockglobal to toggle blocked global chat");
                }

                /*

                
                BEN,
                WE CAN ADD EMOTICONS FROM UNICODE. LOOK IT UP, AND DON'T TELL
                ANYONE. I DON'T HAVE TIME NOW, MAYBE LATER TONIGHT. ALSO, WE SHOULD
                ADD BOLD AND ITALICS, AND CLEAN UP THE CODE FOR ALL OF THAT BY MAKING
                A MODIFIER STRING THAT HAS ALL THE UNICODE ESCAPE CHARACTERS FOR
                BOLD, COLOR, EMOTICONS, ETC. 

                -WILL, 7:51PM
                
                DIFFERENT COLORS FOR INCOMING AND OUTGOING CHAT
                
                List of things to add:
               // emoticons (BM) DONE
                fix defoul (BM) DONE
                \fieldgod (WR) DONE
                fix color (BM) DONE
                fix shortcuts (BM) DONE
                voice (WR) NOT GONNA HAPPEN
                message sounds (WR) DONE
                remove users from list (BM) DONE
                no repeat names (BM) DONE
				bold (WR) DONE
                 */
                else if(fromClient.indexOf("\\whisper ")>=0) {
                    String other = fromClient.substring(fromClient.indexOf(" ")+1,fromClient.indexOf(" ",10));
                    mostRecentChat = other;
                    String message = fromClient.substring(fromClient.indexOf(" ",10));
                    for(ChatThread c:ChatThread.threads) {
                        if(c.name.equals(other)) {
                            c.mostRecentChat = this.name;
                            c.out.println("Whisper from "+this.name+":"+ message);
							this.mostRecentChat = c.name;
                        }
                    }
                }
                else if (fromClient.indexOf("\\shrek")>=0){
                    if(!muted)
						shrek.playSoundOnce();
				}
					
				else if(fromClient.indexOf("\\fieldgod")>=0){
					JFrame editorFrame = new JFrame("Fieldgod");
					editorFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

					BufferedImage image = null;
					try
					{
					  image = ImageIO.read(new File("feldmancolor.jpg"));
					}
					catch (Exception e)
					{
					  e.printStackTrace();
					  System.exit(1);
					}
					ImageIcon imageIcon = new ImageIcon(image);
					JLabel jLabel = new JLabel();
					jLabel.setIcon(imageIcon);
					editorFrame.getContentPane().add(jLabel, BorderLayout.CENTER);

					editorFrame.pack();
					editorFrame.setLocationRelativeTo(null);
					editorFrame.setVisible(true);
				}

                else if(fromClient.indexOf("\\cr ")>=0){
                    String rn = null;
                    String rp = null;
                    if(fromClient.indexOf(" ", 4)>-1){
                        rn = fromClient.substring(fromClient.indexOf(" ")+1,fromClient.indexOf(" ",4));
                        rp = fromClient.substring(fromClient.indexOf(" ",4)+1);
                    }
                    else
                        rn = fromClient.substring(fromClient.indexOf(" ")+1);

                    this.cr(rn,rp);
                }

                else if(fromClient.indexOf("\\lr")>=0)
                    this.lr();

                else if(fromClient.indexOf("\\jr")>=0){
                    String rn = null;
                    String rp = null;
                    if(fromClient.indexOf(" ",4)>-1){
                        rn = fromClient.substring(fromClient.indexOf(" ")+1,fromClient.indexOf(" ",4));
                        rp = fromClient.substring(fromClient.indexOf(" ",4)+1);
                    }
                    else
                        rn = fromClient.substring(fromClient.indexOf(" ")+1);

                    this.jr(rn,rp);
                }

                else if (fromClient.indexOf("\\gr")>=0&&fromClient.indexOf("\\green")<0)
                    this.gr();
				else if (fromClient.indexOf("\\blockglobal")>=0)
					globalBlocked=!globalBlocked;
				else if (fromClient.indexOf("\\mute")>=0) {
					muted = !muted;
				}
                else if (fromClient.indexOf("\\blue2")>=0) {
                    color2="\u001B[34m";
                    this.out.print(color);
                }

                else if (fromClient.indexOf("\\red2")>=0) {
                    color2="\u001B[31m";
                    this.out.print(color);
                }

                else if (fromClient.indexOf("\\green2")>=0) {
                    color2="\u001B[32m";
                    this.out.print(color);
                }

                else if (fromClient.indexOf("\\white2")>=0) {
                    color2="\u001B[37m";
                    this.out.print(color);
                }

                else if (fromClient.indexOf("\\purple2")>=0) {
                    color2="\u001B[35m";
                    this.out.print(color);
                }
				
                else if (fromClient.indexOf("\\cyan2")>=0) {
                    color2="\u001B[36m";
                   // this.out.print(color);
                }
                else if (fromClient.indexOf("\\blue")>=0) {
                    color="\u001B[34m";
                    out.println(color);
                }

                else if (fromClient.indexOf("\\red")>=0) {
                    color="\u001B[31m";
                    out.println(color);
                }

                else if (fromClient.indexOf("\\green")>=0) {
                    color="\u001B[32m";
                    out.println(color);
                }

                else if (fromClient.indexOf("\\white")>=0) {
                    color="\u001B[37m";
                    out.println(color);
                }

                else if (fromClient.indexOf("\\purple")>=0) {
                    color="\u001B[35m";
                    out.println(color);
                }

                else if (fromClient.indexOf("\\cyan")>=0) {
                    color="\u001B[36m";
                    out.println(color);
                }
                else if (fromClient.indexOf("\\sc")>=0) {
                    fromClient = fromClient.substring(4);
                    shortcuts.add(fromClient.substring(0, fromClient.indexOf(" ")));
                    System.out.println(shortcuts);
                    fromClient = fromClient.substring(fromClient.indexOf(" ")+1);
                    shortcuts2.add(fromClient);
                    System.out.println(shortcuts2);
                }
                else {
					System.out.println("Its alive");
                    Message m = new Message(fromClient,this.name);
                    for (ChatThread c : ChatThread.threads) {
                        String messagePrint = m.message;
						boolean gb = false;
						if (!c.globalBlocked && messagePrint.indexOf("\\global")==0) {
							messagePrint = messagePrint.substring(7);
							gb = true;
						}
                        if ((!c.name.equals(m.name)&&c.room.equals(this.room))||gb) {
                            for (int i = 0; i<shortcuts.size(); i++) {
                                int index = 0;
                                while (messagePrint.indexOf(shortcuts.get(i))>=index) {
                                    messagePrint = messagePrint.substring(0, messagePrint.indexOf(shortcuts.get(i)))+shortcuts2.get(i)+messagePrint.substring(messagePrint.indexOf(shortcuts.get(i))+shortcuts.get(i).length());
                                    index = messagePrint.indexOf(shortcuts2.get(i))+shortcuts2.get(i).length()+1;
                                }
                            }
                             if (!c.foul) {
                                while(messagePrint.indexOf("fuck")>=0) {
                                    messagePrint = messagePrint.substring(0, messagePrint.indexOf("fuck"))+"****"+messagePrint.substring(messagePrint.indexOf("fuck")+4);
                                }
                                while(messagePrint.indexOf("Fuck")>=0) {
                                    messagePrint = messagePrint.substring(0, messagePrint.indexOf("Fuck"))+"****"+messagePrint.substring(messagePrint.indexOf("Fuck")+4);
                                }
                                while(messagePrint.indexOf("ass")>=0) {
                                    messagePrint = messagePrint.substring(0, messagePrint.indexOf("ass"))+"****"+messagePrint.substring(messagePrint.indexOf("ass")+3);
                                }
                                while(messagePrint.indexOf("Ass")>=0) {
                                    messagePrint = messagePrint.substring(0, messagePrint.indexOf("Ass"))+"****"+messagePrint.substring(messagePrint.indexOf("Ass")+3);
                                }
                                while(messagePrint.indexOf("shit")>=0) {
                                    messagePrint = messagePrint.substring(0, messagePrint.indexOf("shit"))+"****"+messagePrint.substring(messagePrint.indexOf("shit")+4);
                                }
                                while(messagePrint.indexOf("Shit")>=0) {
                                    messagePrint = messagePrint.substring(0, messagePrint.indexOf("Shit"))+"****"+messagePrint.substring(messagePrint.indexOf("Shit")+4);
                                }
                            }
							while(messagePrint.indexOf("<3")>=0) {
                                    messagePrint = messagePrint.substring(0, messagePrint.indexOf("<3"))+"♥"+messagePrint.substring(messagePrint.indexOf("<3")+2);
                            }
							while(messagePrint.indexOf(":)")>=0) {
                                    messagePrint = messagePrint.substring(0, messagePrint.indexOf(":)"))+"◕‿◕"+messagePrint.substring(messagePrint.indexOf(":)")+2);
                            }
							while(messagePrint.indexOf(":(")>=0) {
                                    messagePrint = messagePrint.substring(0, messagePrint.indexOf(":("))+"⊙﹏⊙"+messagePrint.substring(messagePrint.indexOf(":(")+2);
                            }
                            boolean nameBlocked = false;
                            if (c.blocked!=null && c.blocked.size()>0) {
                                for (String s : c.blocked){
                                    if (s.equals(this.name)) {
                                        nameBlocked = true;
                                        break;
                                    }
                                }
                            }
							String gbs = "";
							if (gb)
								gbs = "[Global] ";
                            if (!nameBlocked) {
                                c.out.println(c.color2 + gbs + this.name+": "+messagePrint+c.color);
								if (!c.muted) {
									received.playSoundOnce();
								}
							}
							if (!muted)
								sent.playSoundOnce();
                        }
                    }
                    System.out.println(this.name+" said: " + fromClient);
                }
            }
            catch (IOException e) {
                //On exception, stop the thread
                System.out.println("IOException on user " + this.name + ": " + e);
                return;
            }
        }
    }

    /*
     * Creates a room
     * @param n: name of the room
     * @param p: password of the room (leave null if room is public)
     */
    public void cr(String n,String p){
        ChatThread.rooms.add(new Message(n,p));
        this.jr(n,p);
        System.out.println(this.name+" called \\cr");
    }

    /*
     * Joins a specified room
     * @param n: name of the room
     * @param p: password of the room (leave null if room is public)
     */
    public void jr(String n,String p){
        boolean appears = false;
        for (Message m: ChatThread.rooms) {
            if (m.message.equals(n))
                appears=true;
        }
        if (appears) {
                   for(Message m:ChatThread.rooms)
            if(m.message.equals(n))
                if(m.name==null||m.name.equals(p))
                    this.room=n;
                else
                    this.out.println("Wrong password");
                }
        else {
            cr(n,p);
        }
        System.out.println(this.name+" called \\jr");

    }

    /* 
     * Lists all public rooms
     */
    public void lr(){
        for(Message m:ChatThread.rooms)
            if(m.name==null)
                this.out.println(m.message);
        System.out.println(this.name+" called \\lr");
    }

    /*
     * Prints the user's current room
     */
    public void gr(){
        this.out.println(this.room);
        System.out.println(this.name+" called \\gr");
    }

    /*
     * Lists all available colors
     */
    public void lc(){
        this.out.println("\u001B[34mblue");
        this.out.println("\u001B[36mcyan");
        this.out.println("\u001B[32mgreen");
        this.out.println("\u001B[35mpurple");
        this.out.println("\u001B[31mred");
        this.out.println("\u001B[37mwhite");
        this.out.println("u001B[33myellow");
        this.out.print(color);
        System.out.println(this.name+" called \\lc");
    }
}