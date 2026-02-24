//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import java.util.Scanner;

abstract class Bank {
    protected double balance = 2000;

    abstract double getInterestRate();

    public void deposit(Scanner sc) {
        System.out.print("Enter amount to deposit: ");
        double amount = sc.nextDouble();
        balance += amount;
        System.out.println("Deposited: " + amount);
        System.out.println("Updated Balance: " + balance);
    }

    public void withdraw(Scanner sc) {
        System.out.print("Enter the amount you want to withdraw: ");
        double withdrawAmount = sc.nextDouble();

        double deduction = (withdrawAmount * getInterestRate()) / 100;
        double withdrawFinal = withdrawAmount - deduction;

        System.out.println(" Rate of Interest is " + getInterestRate() + "%");
        System.out.println("Amount you will receive: " + withdrawFinal);

        if (withdrawAmount <= balance) {
            balance -= withdrawAmount;
        } else {
            System.out.println("Insufficient balance!");
        }
        System.out.println("Available Balance: " + balance);

    }

    public void checkBalance() {
        System.out.println(getClass().getSimpleName() + " Balance: " + balance);
    }

    public abstract int getPin();
}

class SBI extends Bank {
    double getInterestRate() { return 7; }
  public   int getPin() { return 7485; }
}

class Union extends Bank {
    double getInterestRate() { return 10; }
   public int  getPin() { return 8475; }

}

class PNB extends Bank {
    double getInterestRate() { return 8; }
   public  int getPin() { return 1234; }
}

public class MainBank {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("Choose Bank: 1. SBI  2. PNB  3. Union Bank");
        int bankChoice = sc.nextInt();

        Bank obj = null;
        if (bankChoice == 1) obj = new SBI();
        else if (bankChoice == 2) obj = new PNB();
        else if (bankChoice == 3){obj = new Union();}
        else System.out.println("Wrong choice!");
        int attempts = 0, maxAttempts = 3;

        boolean accessGranted = false;
        int CorrectPin = obj.getPin();
        while (attempts < maxAttempts) {
            System.out.print("🏧Enter Your 4-digit ATM PIN:.🔑 ");
            int enteredPin = sc.nextInt();

            if (enteredPin == CorrectPin) {
                System.out.println("✅ Login successful❕ Welcome to your account❕");
                accessGranted = true;

                int choice;
                do {
                    System.out.println("\nChoose Operation:");
                    System.out.println("1. Deposit");
                    System.out.println("2. Withdraw");
                    System.out.println("3. Check Balance");
                    System.out.println("4. Exit");
                    choice = sc.nextInt();

                    switch (choice) {
                        case 1 -> obj.deposit(sc);
                        case 2 -> obj.withdraw(sc);
                        case 3 -> obj.checkBalance();
                        case 4 -> System.out.println("Exiting..✅\n Thanks for using✅✅!");
                        default -> System.out.println("‼️Invalid choice, try again❗️");
                    }
                } while (choice != 4);

                break; // Exit pin attempts loop once logged in
            } else {
                attempts++;
                System.out.println("❌ Invalid PIN❕ PIN Attempts left: " + (maxAttempts - attempts));
            }
        }

        if (!accessGranted) {
            System.out.println("🚫 🚫YOUR CARD HAS BEEN BLOCKED DUE TO TOO MANY WRONG ATTEMPTS❕");
            System.out.println("Please try later❕");
        }
    }
}
































