package src_bolivar_exposito_fcojavier;

import ontology.Types.ACTIONS;
import java.util.LinkedList;
import java.util.Objects;

public class Node implements Comparable<Node>
{
    private State st;

    private int g;
    private int f;

    private Node parent;
    private ACTIONS lastAction;

    public Node(State st, int g, int f, Node parent, ACTIONS lastAction) {
        this.st = st;
        this.g = g;
        this.f = f;
        this.parent = parent;
        this.lastAction = lastAction;
    }

    public Node(State st, int g, int f) {
        this.st = st;
        this.g = g;
        this.f = f;

        this.parent = null;
        this.lastAction = null;
    }

    /**
     * @brief Get path of actions that led to the current node
     * @return LinkedList of Actions
     */
    public LinkedList<ACTIONS> getPath()
    {
        Node current = this;
        LinkedList<ACTIONS> actions = new LinkedList<>();

        while (current.parent != null)
        {
            actions.addFirst(current.lastAction);
            current = current.parent;
        }

        return actions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return st.equals(node.st);
    }

    @Override
    public int hashCode() {
        return Objects.hash(st);
    }

    public int compareTo(Node o)
    {
        int compare_cost = Integer.compare(f, o.f);

        if (compare_cost == 0)
        {
            return st.compareTo(o.getSt());
        }
        else
            return compare_cost;
    }

    public State getSt() {
        return st;
    }

    public int getG() {
        return g;
    }

    public int getF() {
        return f;
    }
}