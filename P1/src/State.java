package src_bolivar_exposito_fcojavier;

import ontology.Types.ACTIONS;
import tools.Vector2d;

import java.util.*;

public class State implements Comparable<State> {
    private Vector2d position;
    private int orientation;

    public State(Vector2d position, int orientation) {
        this.position = position;
        this.orientation = orientation;
    }

    /**
     * @brief Simulate the consequences of an action in a State
     * @param action Action to apply.
     * @param walls Positions of obstacles.
     * @return The resulting State, if the action is not applicable null
     */
    public State simulateAction(ACTIONS action, HashSet<AbstractMap.SimpleEntry<Double, Double>> walls, Boolean simulate_monster)
    {
        Vector2d new_position = position.copy();
        int new_orientation = orientation;

        if (action == ACTIONS.ACTION_LEFT) {
            if (orientation == 3 || simulate_monster)
                new_position.x--;
            else
                new_orientation = 3;
        }
        else if (action == ACTIONS.ACTION_RIGHT) {
            if (orientation == 1 || simulate_monster)
                new_position.x++;
            else
                new_orientation = 1;
        }
        else if (action == ACTIONS.ACTION_UP) {
            if (orientation == 0 || simulate_monster)
                new_position.y--;
            else
                new_orientation = 0;
        }
        else if (action == ACTIONS.ACTION_DOWN) {
            if (orientation == 2 || simulate_monster)
                new_position.y++;
            else
                new_orientation = 2;
        }

        if (walls.contains(new AbstractMap.SimpleEntry<>(new_position.x, new_position.y)))
            return null;
        else
            return new State(new_position, new_orientation);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State state = (State) o;
        return orientation == state.orientation &&
                Objects.equals(position, state.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position.x, position.y, orientation);
    }



    public Vector2d getPosition() {
        return position;
    }

    public int getOrientation() {
        return orientation;
    }

    @Override
    public int compareTo(State state) {
        int compare_x = Double.compare(position.x, state.position.x);

        if (compare_x == 0)
        {
            int compare_y = Double.compare(position.y, state.position.y);

            if (compare_y == 0)
                return Integer.compare(orientation, state.orientation);
            else
                return  compare_y;
        }
        else
            return compare_x;
    }
}