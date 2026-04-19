import java.util.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BankSystem {

    static Scanner sc = new Scanner(System.in);

    static String ACCOUNT_FILE = "accounts.txt";
    static String TRANSACTION_FILE = "transactions.txt";

    static final String GREEN = "\u001B[32m";
    static final String RED = "\u001B[31m";
    static final String RESET = "\u001B[0m";

    public static void main(String[] args) throws Exception {

        titleAnimation();
        createFiles();


        int choice;

        do {

            dashboard();

            System.out.print("Choice: ");
            choice = sc.nextInt();
            sc.nextLine();

            switch(choice){

                case 1:
                    createAccount();
                    break;

                case 2:
                    login();
                    break;

                case 3:
                    System.out.println("Thank you for banking with NexaBank.");
                    break;

                default:
                    System.out.println("Invalid option.");
            }

        }while(choice!=3);
    }

    static void dashboard(){

        System.out.println("\n===========================================");
        System.out.println("        NEXABANK TERMINAL SYSTEM");
        System.out.println("===========================================");
        System.out.println("1  Create Account");
        System.out.println("2  Customer Login");
        System.out.println("3  Exit");
        System.out.println("===========================================");
    }

    static void titleAnimation() throws Exception{

        String title = "===== NEXABANK TERMINAL BANKING SYSTEM =====";

        for(char c : title.toCharArray()){
            System.out.print(c);
            Thread.sleep(20);
        }

        System.out.println("\n");
    }

    static void createFiles() throws Exception{

        File acc = new File(ACCOUNT_FILE);
        File trans = new File(TRANSACTION_FILE);

        if(!acc.exists()){

            FileWriter fw = new FileWriter(acc);

            fw.write(String.format(
                    "%-10s %-15s %-8s %-20s %-5s %-15s %-30s %-12s %-10s\n",
                    "ACC_NO","USERNAME","MPIN","NAME","AGE","PHONE","ADDRESS","BALANCE","STATUS"));

            fw.close();
        }

        if(!trans.exists()){

            FileWriter fw = new FileWriter(trans);

            fw.write(String.format(
                    "%-10s %-15s %-12s %-12s %-12s\n",
                    "ACC_NO","DATE","TIME","TYPE","AMOUNT"));

            fw.close();
        }
    }

    static void createAccount() throws Exception{

        System.out.println("\n------ ACCOUNT OPENING FORM ------");

        System.out.print("Full Name: ");
        String name = sc.nextLine().replace(" ","_");

        System.out.print("Age: ");
        int age = sc.nextInt();
        sc.nextLine();

        System.out.print("Phone: ");
        String phone = sc.nextLine();

        System.out.print("Address: ");
        String address = sc.nextLine().replace(" ","_");

        System.out.print("Create Username: ");
        String username = sc.nextLine();

        System.out.print("Initial Deposit: ");
        double balance = sc.nextDouble();
        sc.nextLine();

        int accNo = (int)(Math.random()*900000)+100000;
        String mpin = String.valueOf((int)(Math.random()*9000)+1000);

        FileWriter fw = new FileWriter(ACCOUNT_FILE,true);

        fw.write(String.format(
                "%-10d %-15s %-8s %-20s %-5d %-15s %-30s %-12.2f %-10s\n",
                accNo, username, mpin, name, age, phone, address, balance, "ACTIVE"));

        fw.close();

        recordTransaction(accNo,"DEPOSIT",balance);

        System.out.println("\nAccount Created Successfully!");
        System.out.println("Account Number : "+accNo);
        System.out.println("MPIN : "+mpin);
    }

    static void login() throws Exception{

        System.out.print("Username: ");
        String user = sc.nextLine();

        System.out.print("MPIN: ");
        String pin = sc.nextLine();

        BufferedReader br = new BufferedReader(new FileReader(ACCOUNT_FILE));

        String line;
        boolean found=false;

        while((line=br.readLine())!=null){

            if(line.startsWith("ACC_NO")) continue;

            String data[] = line.trim().split("\\s+");

            if(data.length < 9) continue;

            String username=data[1];
            String mpin=data[2];
            String status=data[8];

            if(username.equals(user) && mpin.equals(pin)){

                found=true;

                if(status.equals("INACTIVE")){
                    System.out.println(RED+"Account is closed."+RESET);
                    br.close();
                    return;
                }

                int accNo=Integer.parseInt(data[0]);
                double balance=Double.parseDouble(data[7]);

                customerMenu(accNo,balance);

                br.close();
                return;
            }
        }

        br.close();

        if(!found)
            System.out.println("Account not found.");
    }

    static void customerMenu(int accNo,double balance) throws Exception{

        int ch;

        do{

            System.out.println("\n----------- CUSTOMER PANEL ------------");
            System.out.println("1  View Balance");
            System.out.println("2  Deposit Money");
            System.out.println("3  Withdraw Money");
            System.out.println("4  Mini Statement");
            System.out.println("5  Close Account");
            System.out.println("6  Logout");
            System.out.println("---------------------------------------");

            System.out.print("Choice: ");
            ch=sc.nextInt();

            if(ch==1){

                System.out.println("Balance : ₹"+balance);
            }

            else if(ch==2){

                System.out.print("Deposit Amount: ");
                double amt=sc.nextDouble();

                balance+=amt;

                updateBalance(accNo,balance);

                recordTransaction(accNo,"DEPOSIT",amt);

                System.out.println(GREEN+"+₹"+amt+" deposited"+RESET);
            }

            else if(ch==3){

                System.out.print("Withdraw Amount: ");
                double amt=sc.nextDouble();

                if(amt<=balance){

                    balance-=amt;

                    updateBalance(accNo,balance);

                    recordTransaction(accNo,"WITHDRAW",amt);

                    System.out.println(RED+"-₹"+amt+" withdrawn"+RESET);

                }else{

                    System.out.println("Insufficient balance");
                }
            }

            else if(ch==4){

                miniStatement(accNo);
            }

            else if(ch==5){

                closeAccount(accNo);
                return;
            }

        }while(ch!=6);
    }

    static void updateBalance(int accNo,double newBal) throws Exception{

        File input=new File(ACCOUNT_FILE);
        File temp=new File("temp.txt");

        BufferedReader br=new BufferedReader(new FileReader(input));
        PrintWriter pw=new PrintWriter(new FileWriter(temp));

        String line;

        while((line=br.readLine())!=null){

            if(line.startsWith("ACC_NO")){
                pw.println(line);
                continue;
            }

            String data[]=line.trim().split("\\s+");

            if(data.length<9){
                pw.println(line);
                continue;
            }

            if(Integer.parseInt(data[0])==accNo){

                pw.println(String.format(
                        "%-10s %-15s %-8s %-20s %-5s %-15s %-30s %-12.2f %-10s",
                        data[0],data[1],data[2],data[3],data[4],data[5],data[6],newBal,data[8]));

            }else{

                pw.println(line);
            }
        }

        br.close();
        pw.close();

        input.delete();
        temp.renameTo(input);
    }

    static void closeAccount(int accNo) throws Exception{

        File input=new File(ACCOUNT_FILE);
        File temp=new File("temp.txt");

        BufferedReader br=new BufferedReader(new FileReader(input));
        PrintWriter pw=new PrintWriter(new FileWriter(temp));

        String line;

        while((line=br.readLine())!=null){

            if(line.startsWith("ACC_NO")){
                pw.println(line);
                continue;
            }

            String data[]=line.trim().split("\\s+");

            if(data.length<9){
                pw.println(line);
                continue;
            }

            if(Integer.parseInt(data[0])==accNo){

                pw.println(String.format(
                        "%-10s %-15s %-8s %-20s %-5s %-15s %-30s %-12s %-10s",
                        data[0],data[1],data[2],data[3],data[4],data[5],data[6],data[7],"INACTIVE"));

                System.out.println(RED+"Account Closed."+RESET);

            }else{

                pw.println(line);
            }
        }

        br.close();
        pw.close();

        input.delete();
        temp.renameTo(input);
    }

    static void recordTransaction(int accNo,String type,double amount) throws Exception{

        FileWriter fw=new FileWriter(TRANSACTION_FILE,true);

        LocalDateTime now=LocalDateTime.now();

        DateTimeFormatter d=DateTimeFormatter.ofPattern("dd-MM-yyyy");
        DateTimeFormatter t=DateTimeFormatter.ofPattern("HH:mm:ss");

        fw.write(String.format(
                "%-10d %-15s %-12s %-12s %-12.2f\n",
                accNo,now.format(d),now.format(t),type,amount));

        fw.close();
    }

    static void miniStatement(int accNo) throws Exception{

        BufferedReader br=new BufferedReader(new FileReader(TRANSACTION_FILE));

        String line;

        System.out.println("\n-------- MINI STATEMENT --------");
        System.out.println("ACC_NO     DATE           TIME         TYPE        AMOUNT");

        while((line=br.readLine())!=null){

            if(line.startsWith("ACC_NO")) continue;

            String data[]=line.trim().split("\\s+");

            if(data.length<5) continue;

            if(Integer.parseInt(data[0])==accNo){

                System.out.println(line);
            }
        }

        br.close();
    }
}