/**
 * Created by akshatgoyal on 11/18/16.
 */

import java.io.*;
import java.net.Socket;
import java.net.InetAddress;
import java.lang.Thread;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Stack;


public class Battleship {
    public static String API_KEY = "891700819"; ///////// PUT YOUR API KEY HERE /////////
    public static String GAME_SERVER = "battleshipgs.purduehackers.com";

    //////////////////////////////////////  PUT YOUR CODE HERE //////////////////////////////////////

    class Cell {
        public int x,y;
        public boolean flag = false;

        public Cell(int x,int y) {
            this.x = x;
            this.y = y;
        }
    }




    char[] letters;
    int[][] grid;
    Stack<Cell> temp = new Stack<Cell>();
    ArrayList<Cell> t = new ArrayList<Cell>();

    void placeShips(String opponentID) {
        // Fill Grid With -1s
        for(int i = 0; i < grid.length; i++) { for(int j = 0; j < grid[i].length; j++) grid[i][j] = -1; }

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if ((i % 2 == 0 && j % 2== 0) || (i%2 == 1 && j%2 ==1)) {
                    Cell c = new Cell(i,j);
                    t.add(c);
                }
            }
        }

        // Place Ships
        placeDestroyer("G1", "H1");
        placeSubmarine("C1", "C3");
        placeCruiser("H4", "H6");
        placeBattleship("B7", "E7");
        placeCarrier("E0", "E4");
    }

    void makeMove() {
        if (temp.empty()) {
            boolean mademove = false;
            while (!mademove) {
                Random r = new Random();
                int index = r.nextInt(t.size());
                Cell c = t.get(index);
                int i = c.x;
                int j = c.y;
                System.out.println(c.x + " , " + c.y);
                if (this.grid[i][j] == -1) {
                    String wasHitSunkOrMiss = placeMove(this.letters[i] + String.valueOf(j));

                    if (wasHitSunkOrMiss.equals("Hit")) {
                        fillArrayList(i, j);
                    }

                    if (wasHitSunkOrMiss.equals("Hit") || wasHitSunkOrMiss.equals("Sunk")) {
                        this.grid[i][j] = 1;
                    } else {
                        this.grid[i][j] = 0;
                    }
                    t.remove(index);
                    mademove = true;
                    return;
                }
                t.remove(index);
            }
        } else {
            boolean makeMove = false;
            while (!temp.isEmpty()) {
                Cell cell = temp.pop();
                System.out.println("Stack = " + cell.x + ", " + cell.y);
                int i = cell.x;
                int j = cell.y;

                if (this.grid[cell.x][cell.y] == -1) {
                    String wasHitSunkOrMiss = placeMove(this.letters[cell.x] + String.valueOf(cell.y));
                    if (wasHitSunkOrMiss.equals("Hit")) {
                        fillArrayList(i, j);
                    }



                    if (wasHitSunkOrMiss.equals("Hit") || wasHitSunkOrMiss.equals("Sunk")) {
                        this.grid[i][j] = 1;
                    } else {
                        this.grid[i][j] = 0;
                    }
                    makeMove = true;
                    break;
                }
            }
            if (!makeMove) {
                Random r = new Random();
                int index = r.nextInt(t.size());
                Cell c = t.get(index);
                int i = c.x;
                int j = c.y;
                System.out.println(c.x + " , " + c.y);
                if (this.grid[i][j] == -1) {
                    String wasHitSunkOrMiss = placeMove(this.letters[i] + String.valueOf(j));

                    if (wasHitSunkOrMiss.equals("Hit")) {
                        fillArrayList(i, j);
                    }

                    if (wasHitSunkOrMiss.equals("Hit") || wasHitSunkOrMiss.equals("Sunk")) {
                        this.grid[i][j] = 1;
                    } else {
                        this.grid[i][j] = 0;
                    }
                    t.remove(index);
                    return;
                }
                t.remove(index);
            }
        }
    }

    void fillArrayList(int i, int j) {
        if (i-1 >=0) {
            Cell toRemove = new Cell(i-1,j);
            Toremove(toRemove);
            temp.push(toRemove);
        }
        if (i+1 < grid.length) {
            Cell toRemove = new Cell(i+1,j);
            Toremove(toRemove);
            temp.push(toRemove);
        }
        if (j-1 >=0) {
            Cell toRemove = new Cell(i,j-1);
            Toremove(toRemove);
            temp.push(toRemove);
        }
        if (j+1 < grid.length) {
            Cell toRemove = new Cell(i,j+1);
            Toremove(toRemove);
            temp.push(toRemove);
        }
    }

    public void Toremove(Cell c) {
        for (int i = 0; i < t.size(); i++) {
            if (t.get(i).x == c.x && t.get(i).y == c.y) {
                t.remove(i);
                break;
            }
        }
    }

    ////////////////////////////////////// ^^^^^ PUT YOUR CODE ABOVE HERE ^^^^^ //////////////////////////////////////

    Socket socket;
    String[] destroyer, submarine, cruiser, battleship, carrier;

    String dataPassthrough;
    String data;
    BufferedReader br;
    PrintWriter out;
    Boolean moveMade = false;

    public Battleship() {
        this.grid = new int[8][8];
        for(int i = 0; i < grid.length; i++) { for(int j = 0; j < grid[i].length; j++) grid[i][j] = -1; }
        this.letters = new char[] {'A','B','C','D','E','F','G','H'};

        destroyer = new String[] {"A0", "A0"};
        submarine = new String[] {"A0", "A0"};
        cruiser = new String[] {"A0", "A0"};
        battleship = new String[] {"A0", "A0"};
        carrier = new String[] {"A0", "A0"};
    }

    void connectToServer() {
        try {
            InetAddress addr = InetAddress.getByName(GAME_SERVER);
            socket = new Socket(addr, 23345);
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.print(API_KEY);
            out.flush();
            data = br.readLine();
        } catch (Exception e) {
            System.out.println("Error: when connecting to the server...");
            socket = null;
        }

        if (data == null || data.contains("False")) {
            socket = null;
            System.out.println("Invalid API_KEY");
            System.exit(1); // Close Client
        }
    }



    public void gameMain() {
        while(true) {
            try {
                if (this.dataPassthrough == null) {
                    this.data = this.br.readLine();
                }
                else {
                    this.data = this.dataPassthrough;
                    this.dataPassthrough = null;
                }
            } catch (IOException ioe) {
                System.out.println("IOException: in gameMain");
                ioe.printStackTrace();
            }
            if (this.data == null) {
                try { this.socket.close(); }
                catch (IOException e) { System.out.println("Socket Close Error"); }
                return;
            }

            if (data.contains("Welcome")) {
                String[] welcomeMsg = this.data.split(":");
                placeShips(welcomeMsg[1]);
                if (data.contains("Destroyer")) { // Only Place Can Receive Double Message, Pass Through
                    this.dataPassthrough = "Destroyer(2):";
                }
            } else if (data.contains("Destroyer")) {
                this.out.print(destroyer[0]);
                this.out.print(destroyer[1]);
                out.flush();
            } else if (data.contains("Submarine")) {
                this.out.print(submarine[0]);
                this.out.print(submarine[1]);
                out.flush();
            } else if (data.contains("Cruiser")) {
                this.out.print(cruiser[0]);
                this.out.print(cruiser[1]);
                out.flush();
            } else if (data.contains("Battleship")) {
                this.out.print(battleship[0]);
                this.out.print(battleship[1]);
                out.flush();
            } else if (data.contains("Carrier")) {
                this.out.print(carrier[0]);
                this.out.print(carrier[1]);
                out.flush();
            } else if (data.contains( "Enter")) {
                this.moveMade = false;
                this.makeMove();
            } else if (data.contains("Error" )) {
                System.out.println("Error: " + data);
                System.exit(1); // Exit sys when there is an error
            } else if (data.contains("Die" )) {
                System.out.println("Error: Your client was disconnected using the Game Viewer.");
                System.exit(1); // Close Client
            } else {
                System.out.println("Received Unknown Response:" + data);
                System.exit(1); // Exit sys when there is an unknown response
            }
        }
    }

    void placeDestroyer(String startPos, String endPos) {
        destroyer = new String[] {startPos.toUpperCase(), endPos.toUpperCase()};
    }

    void placeSubmarine(String startPos, String endPos) {
        submarine = new String[] {startPos.toUpperCase(), endPos.toUpperCase()};
    }

    void placeCruiser(String startPos, String endPos) {
        cruiser = new String[] {startPos.toUpperCase(), endPos.toUpperCase()};
    }

    void placeBattleship(String startPos, String endPos) {
        battleship = new String[] {startPos.toUpperCase(), endPos.toUpperCase()};
    }

    void placeCarrier(String startPos, String endPos) {
        carrier = new String[] {startPos.toUpperCase(), endPos.toUpperCase()};
    }

    String placeMove(String pos) {
        if(this.moveMade) { // Check if already made move this turn
            System.out.println("Error: Please Make Only 1 Move Per Turn.");
            System.exit(1); // Close Client
        }
        this.moveMade = true;

        this.out.print(pos);
        out.flush();
        try { data = this.br.readLine(); }
        catch(Exception e) { System.out.println("No response after from the server after place the move"); }

        if (data.contains("Hit")) return "Hit";
        else if (data.contains("Sunk")) return "Sunk";
        else if (data.contains("Miss")) return "Miss";
        else {
            this.dataPassthrough = data;
            return "Miss";
        }
    }

    public static void main(String[] args) {
        Battleship bs = new Battleship();
        while(true) {
            bs.connectToServer();
            if (bs.socket != null) bs.gameMain();
        }
    }
}
