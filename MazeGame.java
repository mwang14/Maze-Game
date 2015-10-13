import java.awt.Color;
import java.util.*;

import tester.*;
import javalib.impworld.*;
import javalib.colors.*;
import javalib.worldimages.*;

// Assignment 10
// Wang Michael
// mwang14
// Kim Michael
// mikekim

//represents a Cell class
class Cell {
    int x;
    int y;
    String name;
    boolean visited;
    boolean playervisited;
    ArrayList<Edge> outEdges;
    boolean shortestPath;

    Cell(int x, int y, String name) {
        this.x = x;
        this.y = y;
        this.name = name;
        this.visited = false;
        this.playervisited = false;
        outEdges = new ArrayList<Edge>();
        this.shortestPath = false;
    }


//makes an image of the Cell
    public WorldImage makeImage(int size) {
        if (!visited) {
            return new RectangleImage(new Posn(this.x * size + (size / 2),
                    this.y * size + (size / 2)), size, size, new Color(185, 185,
                            185));
        }
        else if (shortestPath) {
            return new RectangleImage(new Posn(this.x * size + (size / 2),
                    this.y * size + (size / 2)), size, size, new Color(0, 127,
                            255));
        }
        else {
            return new RectangleImage(new Posn(this.x * size + (size / 2),
                    this.y * size + (size / 2)), size, size, new Color(153, 204, 255));
        }
    }
}

//represents an Edge class
class Edge {
    Cell cell1;
    Cell cell2;
    int weight;

    Edge(Cell cell1, Cell cell2) {
        this.cell1 = cell1;
        this.cell2 = cell2;
        this.weight = (new Random()).nextInt(100);
    }
    Edge(Cell cell1, Cell cell2, int weight) {
        this.cell1 = cell1;
        this.cell2 = cell2;
        this.weight = new Random().nextInt(weight + 1);
    }
    

//draws the edge
    public WorldImage drawWall(int scale) {
        Posn start;
        Posn end;
        if (cell1.x == cell2.x) {
            start = new Posn(cell2.x * scale, cell2.y * scale);
            end = new Posn(cell2.x * scale + scale, cell2.y * scale);
        }
        else {
            start = new Posn(cell2.x * scale, cell2.y * scale);
            end = new Posn(cell2.x * scale, cell2.y * scale + scale);
        }
        return new LineImage(start, end, new Black());
    }

}

//represents the MazeWorld class
class MazeWorld extends World {
    int height;
    int width;
    ArrayList<ArrayList<Cell>> cells = new ArrayList<ArrayList<Cell>>();
    ArrayList<Edge> drawEdges = new ArrayList<Edge>();
    ArrayList<Edge> edges = new ArrayList<Edge>();
    ArrayList<Edge> walls = new ArrayList<Edge>();
    HashMap<String, String> hashmap = new HashMap<String, String>();
    int scale;
    int weight2;
    Posn player;
    boolean pathvisible;
    String mode;
    ICollection<Cell> worklist;
    Deque<Cell> alreadySeen;
    HashMap<Cell, Cell> m = new HashMap<Cell, Cell>();
    boolean doneSearching = false;
    

    MazeWorld(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = this.originalBoard(width, height);
        if (height > width) {
            this.scale = 1000 / height;
        } 
        else {
            this.scale = 1000 / width;
        }
        this.weight2 = 5;
        this.drawEdges = this.getEdges();
        Collections.sort(this.drawEdges, new EdgeCompare());
        edges = kruskalgorithm(hashmap, drawEdges);
        this.player = new Posn(0, 0);
        this.pathvisible = true;
        this.mode = "a";
        this.alreadySeen = new Deque<Cell>();
    }
    
    //all the key events
    public void onKeyEvent(String ke) {
        if (ke.equals("left") && 
                !wallExists(1, new Posn(player.x - 1, player.y), player)) {
            player.x = player.x - 1;
            cells.get(player.x).get(player.y).playervisited = true;
        }
        else if (ke.equals("right") && 
                !wallExists(2, new Posn(player.x + 1, player.y), player)) {
            player.x = player.x + 1;
            cells.get(player.x).get(player.y).playervisited = true;
        }
        else if (ke.equals("up") && 
                !wallExists(3, new Posn(player.x , player.y - 1), player)) {
            player.y = player.y - 1;
            cells.get(player.x).get(player.y).playervisited = true;
        }
        else if (ke.equals("down") && 
                !wallExists(4, new Posn(player.x, player.y + 1), player)) {
            player.y = player.y + 1;
            cells.get(player.x).get(player.y).playervisited = true;
            
        }
        else if (ke.equals("n")) {
            this.cells = this.originalBoard(width, height);
            this.drawEdges = this.getEdges();
            Collections.sort(this.drawEdges, new EdgeCompare());
            this.walls = new ArrayList<Edge>();
            edges = kruskalgorithm(hashmap, drawEdges);
            this.player = new Posn(0, 0);
            this.mode = "a";
            this.doneSearching = false;
        }
        else if (ke.equals("h")) {
            if (weight2 > 0) {
                weight2 = this.weight2 - 1;
            }
        }
        else if (ke.equals("v")) {
            if (weight2 < 10) {
                weight2 = this.weight2 + 1;
            }
        }
        else if (ke.equals("p")) {
            pathvisible = !pathvisible;
        }
        else if (ke.equals("d")) {
            this.worklist = new Stack<Cell>();
            this.mode = "dfs";
        }
        else if (ke.equals("b")) {
            this.mode = "bfs";
            this.worklist = new Queue<Cell>();
        }
    }
    
    //animates the searches
    public void onTick() {
        if (this.mode.equals("dfs")) {
            this.dfs(cells.get(0).get(0), cells.get(this.width - 1).get(this.height - 1));
        }
        else if (mode.equals("bfs")) {
            this.bfs(cells.get(0).get(0), cells.get(this.width - 1).get(this.height - 1));
        }

    }
    //determines if a Wall exists between the two Posns
    boolean wallExists(int i, Posn p1, Posn p2) {
        if (i == 1) {
            if (p1.x < 0) {
                return true;
            }
            else {
                for (Edge wall : walls) {
                    if (edgeMatch(wall, cells.get(p1.x).get(p1.y), cells.get(p2.x).get(p2.y))) {
                        return true;
                    }
                }
            }
            return false;
        }
        else if (i == 2) {
            if (p1.x > width - 1) {
                return true;
            }
            else {
                for (Edge wall : walls) {
                    if (edgeMatch(wall, cells.get(p1.x).get(p1.y), cells.get(p2.x).get(p2.y))) {
                        return true;
                    }
                }
            }
            return false;
        }
        else if (i == 3) {
            if (p1.y < 0) {
                return true;
            }
            else {
                for (Edge wall : walls) {
                    if (edgeMatch(wall, cells.get(p1.x).get(p1.y), cells.get(p2.x).get(p2.y))) {
                        return true;
                    }
                }
            }
            return false;
        }
        else if (i == 4) {
            if (p1.y > height - 1) {
                return true;
            }
            else {
                for (Edge wall : walls) {
                    if (edgeMatch(wall, cells.get(p1.x).get(p1.y), cells.get(p2.x).get(p2.y))) {
                        return true;
                    }
                }
            }
            return false;
        }
        else {
            return false;
        }
    }
       
    // helps check if there is a wall between two cells
    boolean edgeMatch(Edge e1, Cell c1, Cell c2) {

        if (e1.cell1.name.equals(c1.name) && e1.cell2.name.equals(c2.name)) {
            return true;
        }
        else if (e1.cell1.name.equals(c2.name) && e1.cell2.name.equals(c1.name)) {
            return true;
        }
        return false;  
    }
    
    //Initializes the original board
    ArrayList<ArrayList<Cell>> originalBoard(int width, int height) {
        ArrayList<ArrayList<Cell>> result = new ArrayList<ArrayList<Cell>>();
        String name = "A";
        for (int x = 0; x < width; x++) {
            ArrayList<Cell> temp = new ArrayList<Cell>();
            for (int y = 0; y < height; y++) {
                temp.add(new Cell(x, y, name));
                this.hashmap.put(name, name);
                name = String.valueOf((char) (name.charAt(0) + 1));
            }
            result.add(temp);
        }
        return result;
    }
    
    //checks if the game is over
    public WorldEnd worldEnds() {
        if (this.player.x == this.width - 1 &&
                this.player.y == this.height - 1) {
            return new WorldEnd(true, this.makeImage()
                    .overlayImages(new TextImage(new Posn((width / 2) * scale, 
                            (height / 2) * scale),
                            "Congratz!", 80, new Blue())));
        }
        else {
            return new WorldEnd(false, this.makeImage());
        }
    }

    //Gets the last element in a Hashmap
    String lastElement(String s, HashMap<String, String> hm) {
        while (s != hm.get(s)) {
            s = hm.get(s);
        }
        return s;
    }

    //Creates all the edges in a board
    ArrayList<Edge> getEdges() {
        ArrayList<Edge> result = new ArrayList<Edge>();
        for (int x = 0; x < cells.size(); x++) {
            for (int y = 0; y < cells.get(0).size(); y++) {
                if (x <= cells.size() - 2) {
                    result.add(new Edge(cells.get(x).get(y), cells.get(x + 1)
                            .get(y), this.weight2));
                }
                if (y <= cells.get(0).size() - 2) {
                    result.add(new Edge(cells.get(x).get(y), cells.get(x).get(
                            y + 1), 10 - this.weight2));
                }
            }
        }
        return result;
    }
    
    //Implements Kruskal's Algorithm to make the maze
    ArrayList<Edge> kruskalgorithm(HashMap<String, String> representatives,
            ArrayList<Edge> workList) {
        ArrayList<Edge> edgesInTree = new ArrayList<Edge>();
        while (workList.size() > 0) {
            Edge e = workList.get(0);
            if (lastElement(e.cell1.name, representatives).equals(
                    lastElement(e.cell2.name, representatives))) {
                this.walls.add(e);
                workList.remove(0);
            }
            else {
                edgesInTree.add(e);
                e.cell1.outEdges.add(e);
                e.cell2.outEdges.add(new Edge(e.cell2, e.cell1, e.weight));
                String s1 = lastElement(e.cell1.name, representatives);
                String s2 = lastElement(e.cell2.name, representatives);
                representatives.put(s1, s2);
                workList.remove(0);
            }
        }
        

        return edgesInTree;
    }

    //flattens a 2D ArrayList into a 1D ArrayList
    ArrayList<Cell> flatten(ArrayList<ArrayList<Cell>> alc) {
        ArrayList<Cell> flat = new ArrayList<Cell>();
        for (ArrayList<Cell> al : alc) {
            for (Cell c : al) {
                flat.add(c);
            }
        }
        return flat;
    }
    
    //Creates the image of the MazeWorld
    public WorldImage makeImage() {
        WorldImage temp = new RectangleImage(new Posn(500, 500), 1000, 1000,
                new White());
        for (Cell c : flatten(cells)) {
            temp = temp.overlayImages(c.makeImage(scale));
        }
        WorldImage layer1 =  new RectangleImage(new Posn(500, 500), 1000, 1000,
                new Color(0, 0, 0, 0));
        layer1 = layer1.overlayImages(new RectangleImage(new Posn(
                scale / 2, scale / 2), scale, scale, new Blue())).overlayImages( 
                new RectangleImage(new Posn((width - 1) * scale + (scale / 2),
                        (height - 1) * scale + (scale / 2)), scale, scale, 
                        new Color(230, 0, 0)));
        for (Cell c : flatten(cells)) {
            if (c.playervisited && pathvisible) {
                layer1 = new OverlayImages(layer1, new RectangleImage(
                        new Posn(c.x * scale + (scale / 2), c.y * scale + (scale / 2)),
                        scale, scale, new Color(135, 255, 139)));
            
            
            }
        }
        
        for (Edge wall : walls) {
            layer1 = new OverlayImages(layer1, wall.drawWall(scale));
        }
        
        // Creates a line at the bottom and right of the maze
        temp = new OverlayImages(temp, new LineImage(
                new Posn(0, height * scale), new Posn(width * scale, height
                        * scale), new Black()));
        temp = new OverlayImages(temp, new LineImage(
                new Posn(width * scale, 0), new Posn(width * scale, height
                        * scale), new Black()));
        
        //creates image of player
        
        temp = temp.overlayImages(layer1);
        temp = new OverlayImages(temp, new DiskImage(new Posn(player.x
                * scale + scale / 2, player.y * scale + scale / 2),
                scale / 4, new Yellow()));
        return temp;
    }
    

    
    // breadth first search
    void bfs(Cell from, Cell to) {
        
        this.worklist.add(from);
        
        if (!this.worklist.isEmpty() && !this.doneSearching) {
            
            Cell next = this.worklist.remove();
            next.visited = true;
            
            if (next.equals(to)) {
                reconstruct(this.m, next);
                this.doneSearching = true;
                
            }
            else if (this.alreadySeen.contains(next)) {
                // do nothing: we've already seen this one
            }
            else {
                for (Edge e : next.outEdges) {
                    if (e.cell1 == next) {
                        this.worklist.add(e.cell2);
                        if (!this.m.containsKey(e.cell2)) {
                            this.m.put(e.cell2, next);
                        }
                    }
                    else {
                        this.worklist.add(e.cell1);
                        if (!this.m.containsKey(e.cell1)) {
                            this.m.put(e.cell1, next);
                        }
                    }
                }
                this.alreadySeen.addAtHead(next);
            }
        }
    }
    
    // changes the cell of each parent to one of the shortest path
    void reconstruct(HashMap<Cell, Cell> m, Cell n) {
        while (n != cells.get(0).get(0) || n == cells.get(this.width - 1).get(this.height - 1)) {
            n.shortestPath = true;
            n = m.get(n);
        }
    }
    
    // depth first search
    /* NOTE:
     * We could not get depth first search to be animated like breadth first
     * search for reasons unknown. The code for getting depth first to animate
     * has been provided as a commented out section in depthHelp.
     */
    void dfs(Cell from, Cell to) {
        
        this.depthHelp(from, to);
        
        if (this.doneSearching) {
            while (!this.worklist.isEmpty()) {
                this.worklist.remove().shortestPath = true;
            }
        }
    }
    
    //Helper Method for dfs
    void depthHelp(Cell from, Cell to) {
        
        this.worklist.add(from);
        while (!this.worklist.isEmpty() && !this.doneSearching) {

            Cell next = this.worklist.remove();
            next.visited = true;
            if (next.equals(to)) {
                this.doneSearching = true;
            }
            else if (this.alreadySeen.contains(next)) {
                this.doneSearching = false;
            }
            else {

                for (Edge e : next.outEdges) {
                    this.worklist.add(e.cell1);
                    this.worklist.add(e.cell2);
                }
                this.alreadySeen.addAtHead(next);
            }
        }
        /*
        if (!this.worklist.isEmpty() && !this.doneSearching) {

            Cell next = this.worklist.remove();
            next.visited = true;
            if (next.equals(to)) {
                this.doneSearching = true;
            }
            else if (this.alreadySeen.contains(next)) {
                
            }
            else {

                for (Edge e : next.outEdges) {
                    this.worklist.add(e.cell1);
                    this.worklist.add(e.cell2);
                }
                this.alreadySeen.addAtHead(next);
            }
        }
         */
    }
}

//Compares two edges by weight
class EdgeCompare implements Comparator<Edge> {
    public int compare(Edge edge1, Edge edge2) {
        if (edge1.weight < edge2.weight) {
            return -1;
        } 
        else if (edge1.weight == edge2.weight) {
            return 0;
        } 
        else {
            return 1;
        }
    }
}

//Represents a mutable collection of items
interface ICollection<T> {
    // Is this collection empty?
    boolean isEmpty();
    // EFFECT: adds the item to the collection
    void add(T item);
    // Returns the first item of the collection
    // EFFECT: removes that first item
    T remove();
}

//represents a queue
class Queue<T> implements ICollection<T> {
    Deque<T> contents;
    Queue() {
        this.contents = new Deque<T>();
    }
 
    // adds an item to the tail of this queue
    public void add(T item) {
        this.contents.addAtTail(item);
    }
 
    // is this queue empty?
    public boolean isEmpty() {
        return this.contents.isEmpty();
    }
 
    // returns the item at the head of this queue
    // EFFECT: removes the item
    public T remove() {
        return this.contents.removeFromHead();
    }
}

//represents a stack
class Stack<T> implements ICollection<T> {
    Deque<T> contents;
    Stack() {
        this.contents = new Deque<T>();
    }

    // is the stack empty
    public boolean isEmpty() {
        return this.contents.isEmpty();
    }
 
    // returns the item at the head of this stack
    // EFFECT: removes the item
    public T remove() {
        return this.contents.removeFromHead();
    }
 
    // adds an item to the head of the stack
    public void add(T item) {
        this.contents.addAtHead(item);
    }
}

//represents an IPredicate
interface IPred<T> {
    boolean apply(T t);
}

//represents a Deque
class Deque<T> {
    Sentinel<T> header;

    Deque() {
        this.header = new Sentinel<T>();
    }

    Deque(Sentinel<T> header) {
        this.header = header;
    }

    // returns size of this deque
    int size() {
        return header.next.size();
    }

    // adds node with given data to the front of this deque
    void addAtHead(T data) {
        Node<T> nextnode = new Node<T>(data);
        header.addAt(nextnode, header.next, header);
    }

    // adds node with give data to the back of this deque
    void addAtTail(T data) {
        Node<T> prevnode = new Node<T>(data);
        header.addAt(prevnode, header, header.prev);
    }

    // removes the first node
    T removeFromHead() {
        if (header.next == null) {
            throw new RuntimeException();
        } 
        else {
            return header.next.removeFrom(header.next);
        }
    }

    // removes the last node
    T removeFromTail() {
        if (header.prev == null) {
            throw new RuntimeException();
        } 
        else {
            return header.next.removeFrom(header.prev);
        }
    }

    // finds first node which pred applies to
    ANode<T> find(IPred<T> pred) {
        return header.next.findhelp(pred);
    }

    // removes given node
    void removeNode(ANode<T> node) {
        header.next.removeAt(node);
    }
  
    boolean isEmpty() {
        return this.size() == 0;
    }
  
    boolean contains(T item) {
        return this.header.next.contains(item);
    }

}

//represent either a Node or a Sentinel
abstract class ANode<T> {
    ANode<T> next;
    ANode<T> prev;

    // returns size of this ANode<T>
    public abstract int size();

    // helper function for find(), returns first ANode<T> that satisfies pred
    abstract ANode<T> findhelp(IPred<T> pred);

    // Adds an ANode into a deque
    public void addAt(ANode<T> node, ANode<T> next, ANode<T> prev) {
        node.next = next;
        node.prev = prev;
        next.prev = node;
        prev.next = node;
    }

    // removes an ANode from a deque, returns the data of removed node
    public abstract T removeFrom(ANode<T> node);

    // removes an ANode
    public abstract void removeAt(ANode<T> node);
  
    public abstract boolean isNode();
  
    public abstract boolean contains(T item);
}

//represents a Sentinel
class Sentinel<T> extends ANode<T> {


    Sentinel() {
        super.next = this;
        super.prev = this;
    }

    // returns size of sentinel
    public int size() {
        return 0;
    }

    // helps the method find
    public ANode<T> findhelp(IPred<T> pred) {
        return this;

    }

    // helps method removeFrom, returns empty string
    public T removeFrom(ANode<T> node) {
        node.next.prev = node.prev;
        node.prev.next = node.next;
        return null;
    }

    // removes node
    public void removeAt(ANode<T> node) {
        throw new RuntimeException();
    }
  
    public boolean isNode() {
        return false;
    }
  
    public boolean contains(T item) {
        return false;
    }

}

//represents a node
class Node<T> extends ANode<T> {
    T data;

    Node(T data) {
        super.next = null;
        super.prev = null;
        this.data = data;
    }

    Node(T data, ANode<T> next, ANode<T> prev) {
        if (next == null || prev == null) {
            throw new IllegalArgumentException();
        }
        super.next = next;
        super.prev = prev;
        this.data = data;
        super.next.prev = this;
        super.prev.next = this;
    }

    // returns size of node, recurses on next node
    public int size() {
        return 1 + this.next.size();
    }

    // helps the method find, returns this node if pred applies,
    // if not, then goes to next node
    public ANode<T> findhelp(IPred<T> pred) {
        if (pred.apply(this.data)) {
            return this;
        } 
        else {
            return this.next.findhelp(pred);
        }
    }

    // removes this node, returns data as string
    public T removeFrom(ANode<T> node) {
        node.next.prev = node.prev;
        node.prev.next = node.next;
        return this.data;
    }

    // removes this node
    public void removeAt(ANode<T> node) {
        node.next.prev = node.prev;
        node.prev.next = node.next;
    }
  
    public boolean isNode() {
        return true;
    }
  
    public boolean contains(T item) {
        return this.data.equals(item) ||
                this.next.contains(item);
    }
}

// represents tests and examples for maze
class ExamplesMaze {
    
    void testRun(Tester t) {
        MazeWorld mw = new MazeWorld(10, 10);
        mw.bigBang(1000, 1000, 0.001);
    }
    

    // Examples of Cells
    Cell cell1 = new Cell(0, 0, "A");
    Cell cell2 = new Cell(0, 1, "B");
    Cell cell3 = new Cell(1, 0, "F");
    Cell cell4 = new Cell(2, 0, "K");
    Cell cell5 = new Cell(1, 1, "J");
    Edge edge1 = new Edge(cell1, cell1);
    Edge edge2 = new Edge(cell1, cell2);
    Edge edge3 = new Edge(cell1, cell3);
    Edge edge4 = new Edge(cell3, cell5);
    Edge edge5 = new Edge(cell2, cell5);
    
    ArrayList<Edge> aedges = new ArrayList<Edge>();
    ArrayList<Edge> aedges2 = new ArrayList<Edge>();
    MazeWorld mazeworld = new MazeWorld(10, 10);
    MazeWorld mazeworld2 = new MazeWorld(2, 2);
    MazeWorld mazeworld3 = new MazeWorld(1, 1);
    
    ArrayList<Cell> acells = new ArrayList<Cell>();
    ArrayList<Cell> acells2 = new ArrayList<Cell>();
    ArrayList<Cell> acells3 = new ArrayList<Cell>();
    ArrayList<ArrayList<Cell>> mazecells = new ArrayList<ArrayList<Cell>>();
    
    void initializedData() {
        cell2.visited = true;
        cell3.shortestPath = true;
        cell4.playervisited = true;
        
        acells.add(cell1);
        acells.add(cell2);
        acells.add(cell3);
        acells2.add(cell4);
        acells2.add(cell5);
        mazecells.add(acells);
        mazecells.add(acells2);
        acells3.add(cell1);
        acells3.add(cell2);
        acells3.add(cell3);
        acells3.add(cell4);
        acells3.add(cell5);
        
        aedges.add(edge1);
        aedges.add(edge2);
        aedges2.add(edge2);
        aedges2.add(edge3);
        aedges2.add(edge4);
        aedges2.add(edge5);

        mazeworld.player = new Posn(9, 9);
        mazeworld.scale = 3;
        mazeworld.edges = aedges;
        mazeworld.getEdges();
        
        mazeworld2.edges = mazeworld2.getEdges();
        mazeworld2.scale = 1;
        mazeworld2.getEdges();
        
    }
    
    // tests the makeCellImage method
    boolean testmakeCellImage(Tester t) {
        this.initializedData();
        return t.checkExpect(cell1.makeImage(5), new RectangleImage(new Posn(cell1.x * 5 + (5 / 2),
                        cell1.y * 5 + (5 / 2)), 5, 5, new Color(185, 185,
                            185))) &&
                            t.checkExpect(cell2.makeImage(5), new RectangleImage(
                                    new Posn(cell2.x * 5 + (5 / 2),
                                    cell2.y * 5 + (5 / 2)), 5, 5, new Color(153, 204,
                                            255))) &&
                            t.checkExpect(cell3.makeImage(5), new RectangleImage(
                                    new Posn(cell3.x * 5 + (5 / 2),
                                    cell3.y * 5 + (5 / 2)), 5, 5, new Color(185, 185,
                                            185))) &&
                            t.checkExpect(cell4.makeImage(5), new RectangleImage(
                                    new Posn(cell4.x * 5 + (5 / 2),
                                    cell4.y * 5 + (5 / 2)), 5, 5, new Color(185, 185,
                                            185)));
    }

    // tests the makeEdgeImage method
    boolean testmakeEdgeImage(Tester t) {
        return t.checkExpect(edge2.drawWall(5), new LineImage(new Posn(cell2.x * 5, cell2.y * 5),
                new Posn(5, 5), new Black())) &&
                t.checkExpect(edge1.drawWall(5), new LineImage(
                        new Posn(cell1.x * 5, cell1.y * 5),
                        new Posn(cell1.x * 5 + 5, cell1.y * 5), new Black()));
    }
    
    
    // tests the worldEnds method
    
    boolean testworldEnds(Tester t) {
        this.initializedData();
        return t.checkExpect(mazeworld.worldEnds(), new WorldEnd(true, mazeworld.makeImage()
                .overlayImages(new TextImage(new Posn(5 * 3, 
                        5 * 3),
                        "Congratz!", 80, new Blue()))));
    }
    
    
    // tests the edgeMatch method
    boolean testedgeMatch(Tester t) {
        return t.checkExpect(mazeworld.edgeMatch(edge1, cell1, cell1), true) &&
                t.checkExpect(mazeworld.edgeMatch(edge1, cell2, cell2), false);
    }
    
    // tests the wallExists method
    boolean testwallExists(Tester t) {
        return t.checkExpect(mazeworld.wallExists(1, new Posn(0, 0), new Posn(5, 5)), false) &&
                t.checkExpect(mazeworld2.wallExists(1, new Posn(0, 0), new Posn(0, 0)), false);
    }
    
    // tests the flatten method
    boolean testFlatten(Tester t) {
        return t.checkExpect(mazeworld3.flatten(mazecells), acells3);
    }
}

// represents examples and tests for deque
class ExamplesDeque {

    Node<String> node1 = new Node<String>("abc");
    Node<String> node2 = new Node<String>("bcd");
    Node<String> node4 = new Node<String>("def");
    Node<String> node3 = new Node<String>("cde");

    Node<String> node10 = new Node<String>("abc");
    Node<String> node11 = new Node<String>("bcd");
    Node<String> node12 = new Node<String>("cde");
    Node<String> node13 = new Node<String>("def");

    Node<String> node5 = new Node<String>("abc");
    Node<String> node6 = new Node<String>("bcd");
    Node<String> node7 = new Node<String>("cde");
    Node<String> node8 = new Node<String>("def");
    Node<String> node9 = new Node<String>("efg");
    Sentinel<String> sentinel1 = new Sentinel<String>();
    Sentinel<String> sentinel2 = new Sentinel<String>();
    Sentinel<String> sentinel3 = new Sentinel<String>();
    Sentinel<String> sentinel4 = new Sentinel<String>();
    Deque<String> dq = new Deque<String>(sentinel1);
    Deque<String> dq2 = new Deque<String>(sentinel2);
    Deque<String> dq3 = new Deque<String>(sentinel3);
    Deque<String> dq4 = new Deque<String>(sentinel4);
    Deque<String> mtdq = new Deque<String>();

    void initializedData() {

        node1 = new Node<String>("abc", sentinel1, sentinel1);
        node2 = new Node<String>("bcd", sentinel1, node1);
        node3 = new Node<String>("cde", sentinel1, node2);
        node4 = new Node<String>("def", sentinel1, node3);

        node9 = new Node<String>("wang", sentinel2, sentinel2);
        node5 = new Node<String>("li", sentinel2, node9);
        node6 = new Node<String>("kim", sentinel2, node5);
        node7 = new Node<String>("xu", sentinel2, node6);
        node8 = new Node<String>("spohngellert", sentinel2, node7);

        node10 = new Node<String>("zjk", sentinel4, sentinel4);
        node11 = new Node<String>("xx", sentinel4, node10);
        node12 = new Node<String>("ml", sentinel4, node11);
        node13 = new Node<String>("fzd", sentinel4, node12);
    }

    boolean testAddAtHead(Tester t) {
        initializedData();
        this.dq.addAtHead("aa");

        mtdq.addAtHead("arg");
        Sentinel<String> sentinel6 = new Sentinel<String>();
        Sentinel<String> sentinel7 = new Sentinel<String>();
        Deque<String> emptydq = new Deque<String>(sentinel7);
        ANode<String> node8b = new Node<String>("arg", sentinel7, sentinel7);

        ANode<String> node1b = new Node<String>("aa", sentinel6, sentinel6);
        ANode<String> node2b = new Node<String>("abc", sentinel6, node1b);
        ANode<String> node3b = new Node<String>("bcd", sentinel6, node2b);
        ANode<String> node4b = new Node<String>("cde", sentinel6, node3b);
        ANode<String> node5b = new Node<String>("def", sentinel6, node4b);
        Deque<String> dqb = new Deque<String>(sentinel6);

        return t.checkExpect(dq, dqb) && t.checkExpect(mtdq, emptydq);
    }

    boolean testAddAtTail(Tester t) {
        initializedData();
        dq2.addAtTail("josh");
        Sentinel<String> sentinel7 = new Sentinel<String>();
        ANode<String> node6b = new Node<String>("wang", sentinel7, sentinel7);
        ANode<String> node7b = new Node<String>("li", sentinel7, node6b);
        ANode<String> node8b = new Node<String>("kim", sentinel7, node7b);
        ANode<String> node9b = new Node<String>("xu", sentinel7, node8b);
        ANode<String> node10b = new Node<String>("spohngellert", sentinel7,
                node9b);
        ANode<String> node11b = new Node<String>("josh", sentinel7, node10b);
        Deque<String> dq7 = new Deque<String>(sentinel7);

        return t.checkExpect(dq2, dq7);
    }

    boolean testRemoveFromTail(Tester t) {
        initializedData();
        dq.removeFromTail();
        mtdq.removeFromTail();

        Sentinel<String> sentinel6 = new Sentinel<String>();
        ANode<String> node2b = new Node<String>("abc", sentinel6, sentinel6);
        ANode<String> node3b = new Node<String>("bcd", sentinel6, node2b);
        ANode<String> node4b = new Node<String>("cde", sentinel6, node3b);
        Deque<String> dqb = new Deque<String>(sentinel6);
        Deque<String> emptydq = new Deque<String>();
        return t.checkExpect(dq, dqb) && t.checkExpect(mtdq, emptydq);

    }

    boolean testRemoveFromHead(Tester t) {
        initializedData();
        dq.removeFromHead();
        mtdq.removeFromHead();

        Sentinel<String> sentinel6 = new Sentinel<String>();
        ANode<String> node3b = new Node<String>("bcd", sentinel6, sentinel6);
        ANode<String> node4b = new Node<String>("cde", sentinel6, node3b);
        ANode<String> node5b = new Node<String>("def", sentinel6, node4b);
        Deque<String> dqb = new Deque<String>(sentinel6);
        Deque<String> emptydq = new Deque<String>();
        return t.checkExpect(dq, dqb) && t.checkExpect(mtdq, emptydq);
    }

    boolean testRemoveNode(Tester t) {
        initializedData();
        dq.removeNode(node2);
        Sentinel<String> sentinel6 = new Sentinel<String>();
        ANode<String> node2b = new Node<String>("abc", sentinel6, sentinel6);
        ANode<String> node4b = new Node<String>("cde", sentinel6, node2b);
        ANode<String> node5b = new Node<String>("def", sentinel6, node4b);
        Deque<String> dqb = new Deque<String>(sentinel6);
        return t.checkExpect(dq, dqb);
    }

    boolean testSize(Tester t) {
        initializedData();
        return t.checkExpect(dq.size(), 4) && t.checkExpect(dq2.size(), 5)
                && t.checkExpect(dq3.size(), 0);
    }

}