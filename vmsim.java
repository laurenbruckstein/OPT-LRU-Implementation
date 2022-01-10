import java.lang.*;
import java.util.*; 
import java.io.*;
 
public class vmsim { 
    // Information Variables
    private static String algType;  // LRU or OPT
    private static int frames;      // Number of Frames
    private static int pageSize;    // pageSize

    static int framesP0; // Max size of listP0
    static int framesP1; // Max size of listP1

    // Counter Variables
    private static int memAccesses;   // Number of memory accesses [increment for each line in traceFile]
    private static int pageFaults;    // Number of times a page was not in memory when looking for it
    private static int pageEvictions; // Number of times to write to disk

    // Data Structures 
    private static LinkedList<Node> listP0 = new LinkedList<Node>();  // Use with mode bit 0
    private static LinkedList<Node> listP1 = new LinkedList<Node>();  // Use with mode bit 1

    private static HashMap<Integer, LinkedList<Integer>> htP0; // Hashmap for Process0
    private static HashMap<Integer, LinkedList<Integer>> htP1; // Hashmap for Process1

    public static void main(String[] args){ 

        // Read in commandline args
        algType = args[1]; 
        String fr = args[3];
        frames = Integer.parseInt(fr);
        String ps = args[5]; 
        pageSize = Integer.parseInt(ps);
        String memorySplit = args[7];
        String[] AB = memorySplit.split(":"); 
        int a = Integer.parseInt(AB[0]);
        int b = Integer.parseInt(AB[1]);
        String tf = args[8]; 
        File traceFile = new File(tf);

        // Compute frame size for each processes list
        framesP0 = a*(frames/(a+b));
        framesP1 = frames - framesP0;

        // Decide which algorithm to use based on algType
        if(algType.equals("lru")){
            LRU(traceFile, pageSize);
        } else{
            createHashMap(traceFile, pageSize);
            OPT(traceFile, pageSize);
        }

        // Algorithm Finished, print results
        printSummaryStats();

    }


    // LRU ALGORITHM
    // 1. Scan in traceFile line by line and compute variables
    // 2. Determine which process to work with/ which data structures
    // 3. Search Linked List for the pageNumber 
    // 4. Updated linked List accordingly
        // For evictions, remove the tail of linkedList
    public static void LRU(File traceFile, int pageSize){
        Scanner scan1 = null;
        try{
            scan1 = new Scanner(traceFile);
        }
        catch (FileNotFoundException fnfe){
            System.out.println(fnfe);
        }
        while(scan1.hasNextLine()){ 

            // Parse traceFile
            String line = scan1.nextLine();
            String[] split = line.split(" ");
            int mode = split[0].charAt(0);
            String address = split[1];
            String p = split[2];
            int process = Integer.parseInt(p);
            memAccesses++;
            int pageNum = getPageNum(address, pageSize);

            // Use data strctures for correct process
            LinkedList<Node> list;
            int maxSize;
            if(process == 0){
                list = listP0;
                maxSize = framesP0;
            } else{
                list = listP1;
                maxSize = framesP1;
            }  

            // Search Linked List for pageNum
            int index = -1;
            for(int i = 0; i < list.size(); i++){
                // TESTING: System.out.println("For loop: " + list.get(i).pageNumber);
                if(list.get(i).pageNumber == pageNum){
                    index = i;
                    break;
                }                
            }
            // IF MODE == s, set DIRTY BIT = 1
            int db = 0;
            if(mode == 's'){
                db = 1;
            }
        
            // TESTING: 
            // System.out.println("pageNUm: " + pageNum + " index: " + index + " db: " + db);
            // System.out.println("List size: " + list.size());
            // System.out.println("Max size: " + maxSize);

             // If list is at maxSize, and new page needs to be inserted, evict a page 
            if(list.size() == maxSize && index == -1){
                Node evict = list.getLast();
                int dirty = evict.dirtyBit;
                list.remove(list.getLast());
                // Only writes to disk if dirty bit is set
                if(dirty == 1){
                    pageEvictions++; 
                }
            }  

            if(index == -1){ // If page not there insert at head
                list.addFirst(new Node(pageNum, db));  
                pageFaults++; 
            } else{ // If page was found remove node and reinsert at head
                Node cur = list.get(index);
                int curDirty = cur.dirtyBit;
                if(curDirty == 1){
                    db = 1;
                }
                list.remove(cur);
                list.addFirst(new Node(pageNum, db));
            }
        }
    } 


    // Create Hashmap [For OPT Algorithm]
    // Add pageNums that are not yet in the hashmap
    // Add lineNums to pageNums already existing in hashmap
    public static void createHashMap(File traceFile, int pageSize){

        // Initialize a hashMap for each process
        htP0 = new HashMap<Integer, LinkedList<Integer>>();
        htP1 = new HashMap<Integer, LinkedList<Integer>>();  

        Scanner scan0 = null;
        try{
            scan0 = new Scanner(traceFile);
        }
        catch (FileNotFoundException fnfe){
            System.out.println(fnfe);
        }

        int lineNum = 0;
        while(scan0.hasNextLine()){ 

            // Parse traceFile
            String line = scan0.nextLine();
            String[] split = line.split(" ");
            int mode = split[0].charAt(0);
            String address = split[1];
            String p = split[2];
            int process = Integer.parseInt(p);
            int pageNum = getPageNum(address, pageSize);
 
            // Use hashmap for correct process
            HashMap<Integer, LinkedList<Integer>> ht;
            if(process == 0){
                ht = htP0;
            } else{
                ht = htP1;
            } 

            // Find the pageNum in the hashmap
            LinkedList<Integer> curVal = ht.get(pageNum);

            // If not in hashmap, insert; if found, add lineNum to value
            if(curVal == null){ 
                LinkedList<Integer> value = new LinkedList<Integer>(); 
                value.add(lineNum);
                ht.put(pageNum, value); 
            }  else{
                curVal.add(lineNum);
            }
    
            lineNum++;
        }

        // PRINT HASHMAP FOR TESTING:
        //System.out.println(htP0);
    }
    

    // OPT algorithm
    // 1. Scan in traceFile line by line and compute variables
    // 2. Determine which process to work with/ which data structures
    // 3. Look up pageNum in hashmap and delete head of its LinkedList
    // 4. Search Linked List for the pageNumber 
    // 5. Updated linked List accordingly 
        // For eviction, choose pageNum with the followng priority:
            // a. Most recent with an empty list
            // b. Largest head 
            // c. If a tie between largest head --> LRU
    public static void OPT(File traceFile, int pageSize){

        Scanner scan1 = null;
        try{
            scan1 = new Scanner(traceFile);
        }
        catch (FileNotFoundException fnfe){
            System.out.println(fnfe);
        }
        while(scan1.hasNextLine()){ 
            // Parse traceFile
            String line = scan1.nextLine();
            String[] split = line.split(" ");
            int mode = split[0].charAt(0);
            String address = split[1];
            String p = split[2];
            int process = Integer.parseInt(p);
            int pageNum = getPageNum(address, pageSize);
            memAccesses++; 

            // Use data structures for correct process
            LinkedList<Node> list;
            int maxSize;
            HashMap<Integer, LinkedList<Integer>> ht;
            if(process == 0){
                ht = htP0;
                list = listP0;
                maxSize = framesP0;
            } else{
                ht = htP1;
                list = listP1;
                maxSize = framesP1;
            } 

            // Look up page number in hash table; delete head of value
            LinkedList<Integer> curVal = ht.get(pageNum);
            if (curVal != null){
                curVal.removeFirst();
            } else{
                //print error message
                System.out.println("Error: Value not found");
            }

            // Find current page in linked list [same as LRU]
            int index = -1;
            for(int i = 0; i < list.size(); i++){
                // TESTING: System.out.println("For loop: " + list.get(i).pageNumber);
                if(list.get(i).pageNumber == pageNum){
                    index = i;
                    break;
                }                
            }

            // IF MODE == s, set DIRTY BIT = 1
            int db = 0;
            if(mode == 's'){
                db = 1;
            }

            // TESTING: 
            //System.out.println("NEW LINE --> pageNUm: " + pageNum + " index: " + index + " db: " + db);
            //System.out.println("List size: " + list.size());
            //System.out.println("Max size: " + maxSize);
            //System.out.print("\n");
        
            int largestLineNum = 0;     // Keep track of the largest lineNum [to compare heads]
            int largestPN = 0;          // Keep track of the largest pageNum [need for eviction]
            boolean emptyFound = false; // If an empty list is found, lineNum comparison is not necessary
            if(list.size() == maxSize && index == -1){  // Evict a page if list is at maxSize AND if the current pageNum is not already in the list
                for(int i = 0; i < list.size(); i++){
                    int pn = list.get(i).pageNumber;          // Iterate over each page in LinkedList
                    LinkedList<Integer> curList = ht.get(pn); // Look up each pn in hashtable 
                    if(curList.isEmpty()){ // If the list in the hashtable is empty
                        largestPN = pn; 
                        emptyFound = true;
                    } else{ 
                        int head = curList.get(0); // Get head of linkedlist 
                        if(head > largestLineNum && emptyFound == false){  // If the current head is largest than the largestLineNum [and no empty lists have been found], update.
                            largestLineNum = head;
                            largestPN = pn;
                        }
                        /*else if(head == largestHead){
                            //theres a tie
                        }*/ 
                    }
                }

                // TESTING: System.out.println("EVICTING PAGE: " + largestPN + "\n"); 

                // Evict the victim page [page held in largestPN variable]
                Node evict = null;
                for(int i = 0; i < list.size(); i++){
                    if(list.get(i).pageNumber == largestPN){ // Find the Node for largestPN in the list
                        evict = list.get(i);
                        break;
                    }
                }
                int dirty = evict.dirtyBit; // Check its dirtyBit for pageEvictions
                list.remove(evict);         // Remove node from list
                if(dirty == 1){
                    pageEvictions++;        // Only write to disk if dirty bit is set
                }
            }  
            
            if(index == -1){ // If page not there insert at head
                list.addFirst(new Node(pageNum, db));  
                pageFaults++; 
            } else{ // If page was found remove node and reinsert at head
                Node cur = list.get(index);
                int curDirty = cur.dirtyBit;
                if(curDirty == 1){
                    db = 1;
                }
                list.remove(cur);
                list.addFirst(new Node(pageNum, db));
            }
        }
    }


    // GET THE PAGE NUMBER
    public static int getPageNum(String address, int pageSize){ 
        String ad = address.substring(2);    // Get rid of "0x"
        long add = Long.parseLong(ad, 16);   // Make type long
        int pageOffset = (int) Math.ceil(Math.log(pageSize)/Math.log(2) + 10); // Calculate pageOffSet
        long pn = add>>pageOffset;  // Get the pageNumber portion
        int pageNum = Math.toIntExact(pn);  // Convert to Integer
        return pageNum;
    } 


    // Print Summary Once Done
    public static void printSummaryStats(){ 
        System.out.println("Algorithm: " + algType.toUpperCase());
        System.out.println("Number of frames: " + frames);
        System.out.println("Page size: " + pageSize + " KB");
        System.out.println("Total memory accesses: " + memAccesses);
        System.out.println("Total page faults: " + pageFaults);
        System.out.println("Total writes to disk: " + pageEvictions);
    }

    // Node inner class 
    public static class Node{
        int pageNumber;
        int dirtyBit; 
        public Node(int pageNumber, int dirtyBit){
            this.pageNumber = pageNumber;    
            this.dirtyBit = dirtyBit; 
        }
    }

}
 
