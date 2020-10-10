package src_bolivar_exposito_fcojavier;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

import java.util.*;
import static java.lang.Math.abs;

public class Agent extends AbstractPlayer
{
    private final Vector2d fscale;
    private final int n_gems;
    private Vector2d goal;
    private Vector2d portal;
    
    private HashSet<AbstractMap.SimpleEntry<Double, Double>> walls;
    private HashMap<AbstractMap.SimpleEntry<Double, Double>, Double> dist_gems_avatar;
    private HashMap<AbstractMap.SimpleEntry<Double, Double>, Double> dist_gems_portal;
    private LinkedList<ACTIONS> plan;

    private int hotnessmap[][];
    private int enter_danger_threshold;
    private int exit_danger_threshold;
    private Boolean danger;
    private Random generator = new Random();


    /**
     * @brief initialize all variables for the agent
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     */
    public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        // Factor de escala (pixeles -> grid)
        this.fscale = new Vector2d(stateObs.getWorldDimension().width / stateObs.getObservationGrid().length, stateObs.getWorldDimension().height / stateObs.getObservationGrid()[0].length);
        this.walls = new HashSet<>();
        this.plan = new LinkedList<>();
        this.dist_gems_portal = new HashMap<>();
        this.dist_gems_avatar = new HashMap<>();
        this.portal = scale(stateObs.getPortalsPositions()[0].get(0).position);
        this.n_gems = stateObs.getResourcesPositions() == null ? 0 : 10;
        this.hotnessmap = new int[stateObs.getObservationGrid().length][stateObs.getObservationGrid()[0].length];
        this.enter_danger_threshold = 3;
        this.exit_danger_threshold = 4;
        this.danger = false;

        // Para cada pared guardamos su posición en un HashSet y así comprobar si hay una pared en una posición rápidamente
        // También guardamos el "calor" (peligro) que suma cada pared en una matriz
        for (Observation wall : stateObs.getImmovablePositions()[0]) {
            Vector2d wall_position = scale(wall.position);

            walls.add(new AbstractMap.SimpleEntry<>(wall_position.x, wall_position.y));

            for (int i = 0; i < 2; i++)
            {
                if (wall_position.x + i + 1 > 0 && wall_position.x + i + 1 < stateObs.getObservationGrid().length)
                    hotnessmap[(int) wall_position.x + i + 1][(int) wall_position.y] += 2 - i;
                if (wall_position.x - i - 1 > 0 && wall_position.x - i - 1 < stateObs.getObservationGrid().length)
                    hotnessmap[(int) wall_position.x - i - 1][(int) wall_position.y] += 2 - i;
                if (wall_position.y - i - 1 > 0 && wall_position.y - i - 1 < stateObs.getObservationGrid()[0].length)
                    hotnessmap[(int) wall_position.x][(int) wall_position.y - i - 1] += 2 - i;
                if (wall_position.y + i + 1 > 0 && wall_position.y + i + 1 < stateObs.getObservationGrid()[0].length)
                    hotnessmap[(int) wall_position.x][(int) wall_position.y + i + 1] += 2 - i;
            }

            if (wall_position.x + 1 > 0 && wall_position.x + 1 < stateObs.getObservationGrid().length) {
                if (wall_position.y - 1 > 0 && wall_position.y - 1 < stateObs.getObservationGrid()[0].length)
                    if (hotnessmap[(int) wall_position.x + 1][(int) wall_position.y - 1] == 0)
                        hotnessmap[(int) wall_position.x + 1][(int) wall_position.y - 1] = 1;
                if (wall_position.y + 1 > 0 && wall_position.y + 1 < stateObs.getObservationGrid()[0].length)
                    if (hotnessmap[(int) wall_position.x + 1][(int) wall_position.y + 1] == 0)
                        hotnessmap[(int) wall_position.x + 1][(int) wall_position.y + 1] = 1;
            }
            if (wall_position.x - 1 > 0 && wall_position.x - 1 < stateObs.getObservationGrid().length) {
                if (wall_position.y - 1 > 0 && wall_position.y - 1 < stateObs.getObservationGrid()[0].length)
                    if (hotnessmap[(int) wall_position.x - 1][(int) wall_position.y - 1] == 0)
                        hotnessmap[(int) wall_position.x - 1][(int) wall_position.y - 1] = 1;
                if (wall_position.y + 1 > 0 && wall_position.y + 1 < stateObs.getObservationGrid()[0].length)
                    if (hotnessmap[(int) wall_position.x - 1][(int) wall_position.y + 1] == 0)
                        hotnessmap[(int) wall_position.x - 1][(int) wall_position.y + 1] = 1;
            }
        }

        // Calculamos la distancia de cada gema al portal y la guardamos en un HashMap
        if (n_gems != 0)
            for (Observation gem : stateObs.getResourcesPositions()[0])
            {
                Vector2d gem_position = scale(gem.position);
                int gem_orientation;

                if (portal.x < gem_position.x)
                    gem_orientation = 3;
                else if (portal.x > gem_position.x)
                    gem_orientation = 1;
                else
                    if (portal.y < gem_position.y)
                        gem_orientation = 0;
                    else
                        gem_orientation = 2;

                State gem_st = new State(gem_position, gem_orientation);
                double dist_portal = findPath(new Node(gem_st, 0, manhattanDist(gem_position, portal), null, ACTIONS.ACTION_NIL), portal).size();

                dist_gems_portal.put(new AbstractMap.SimpleEntry<>(gem_position.x, gem_position.y), dist_portal);
            }
    }

    /**
     * @brief Return action to do based on the game objective and a deliberative/reactive behavior
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return 	ACTION to do based on deliberative/reactive behavior
     */
    @Override
    public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        Vector2d avatar_position = scale(stateObs.getAvatarPosition());
        int avatar_orientation = getOrientation(stateObs);

        // Si estamos en peligro o solo hay enemigos decidimos la acción con un comportamiento reactivo
        if (checkDanger(stateObs) || (n_gems == 0 && stateObs.getNPCPositions() != null))
        {
            plan.clear();

            ArrayList<Vector2d> casillas = new ArrayList<>(Arrays.asList(
                    new Vector2d(avatar_position.x, avatar_position.y - 1),
                    new Vector2d(avatar_position.x + 1, avatar_position.y),
                    new Vector2d(avatar_position.x, avatar_position.y + 1),
                    new Vector2d(avatar_position.x - 1, avatar_position.y),
                    avatar_position));
            int hot_min = Integer.MAX_VALUE, index;
            ArrayList<Integer> index_list = new ArrayList<>();

            // Obtenemos que acciones de las posibles tiene menos peligro
            for (int i = 0; i < 5; i++)
            {
                if (!walls.contains(new AbstractMap.SimpleEntry<>(casillas.get(i).x, casillas.get(i).y))) {
                    int hotness = getHotness(casillas.get(i), stateObs);

                    if (hotness < hot_min) {
                        index_list.clear();
                        hot_min = hotness;
                        index_list.add(i);
                    }
                    if (hotness == hot_min)
                        index_list.add(i);
                }
            }

            // Escogemos la acción priorizando aquellas instantaneas, moverse en la misma dirección o quedarse quieto.
            if (index_list.contains(avatar_orientation))
                index = avatar_orientation;
            else if (index_list.contains(4))
                index = 4;
            else
                index = index_list.get(generator.nextInt(index_list.size()));

            switch(index)
            {
                case 0:
                    plan.add(ACTIONS.ACTION_UP);
                    break;
                case 1:
                    plan.add(ACTIONS.ACTION_RIGHT);
                    break;
                case 2:
                    plan.add(ACTIONS.ACTION_DOWN);
                    break;
                case 3:
                    plan.add(ACTIONS.ACTION_LEFT);
                    break;
                case 4:
                    plan.add(ACTIONS.ACTION_NIL);
                    break;
            }
        }
        else if (plan.isEmpty()) {
            int gemas_obtenidas = stateObs.getAvatarResources().get(6) == null ? 0 : stateObs.getAvatarResources().get(6);

            // Si nos quedan gemas por recoger
            if (n_gems != 0 && n_gems > gemas_obtenidas)
            {
                // Calculamos las distancias del avatar a las gemas
                State avatar = new State(avatar_position, avatar_orientation);
                for (Observation gem : stateObs.getResourcesPositions()[0])
                {
                    Vector2d gem_position = scale(gem.position);
                    double dist_avatar = findPath(new Node(avatar, 0, manhattanDist(avatar.getPosition(), gem_position), null, ACTIONS.ACTION_NIL), gem_position).size();

                    dist_gems_avatar.put(new AbstractMap.SimpleEntry<>(gem_position.x, gem_position.y), dist_avatar);
                }

                // Elección heurística de que gemas recoger
                ArrayList<Double> costs = new ArrayList<>(stateObs.getResourcesPositions()[0].size());
                for (int i = 0; i < stateObs.getResourcesPositions()[0].size(); i++)
                    costs.add(gemHeuristicSelection(scale(stateObs.getResourcesPositions()[0].get(i).position), stateObs));

                ArrayList<Observation> selected_gems = new ArrayList<>(n_gems - gemas_obtenidas);
                for (int i = 0; i < n_gems - gemas_obtenidas; ++i) {
                    int index = costs.indexOf(Collections.min(costs));
                    costs.set(index, Double.POSITIVE_INFINITY);
                    selected_gems.add(stateObs.getResourcesPositions()[0].get(index));
                }

                // Elección de orden de gemas
                Observation target_gem = selected_gems.get(0);
                double min_cost = Double.POSITIVE_INFINITY;

                for (Observation gema : selected_gems) {
                    double cost = gemHeuristicOrder(scale(gema.position), stateObs);

                    if (cost < min_cost) {
                        min_cost = cost;
                        target_gem = gema;
                    }
                }
                // Establecemos la gema seleccionada como objetivo
                goal = scale(target_gem.position);
            }
            else
                goal = portal;

            State inicial = new State(avatar_position, avatar_orientation);
            Node initial = new Node(inicial, 0, manhattanDist(inicial.getPosition(), goal), null, null);

            plan = findPath(initial, goal);
        }

        return plan.poll();
    }

    /**
     * @brief A* Algorithm to find optimal path from two points in the map.
     * @param initial Initial Node to start from.
     * @param target Wanted final location.
     * @return 	List of ACTIONS to reach target.
     */
    private LinkedList<ACTIONS> findPath(Node initial, Vector2d target, Boolean simulate_monster)
    {
        ArrayList<ACTIONS> available_actions = new ArrayList<>(Arrays.asList(ACTIONS.ACTION_UP, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_LEFT));
        TreeMap<Node, Node> open = new TreeMap<>();
        HashSet<Node> closed = new HashSet<>();
        Node current;

        open.put(initial, initial);

        while (!open.isEmpty()) {
            current = open.pollFirstEntry().getKey();
            closed.add(current);
            // Si el nodo actual es solución lo devolvemos
            if (current.getSt().getPosition().equals(target)) {
                return current.getPath();
            }
            // Generamos hijos
            for (ACTIONS action : available_actions) {
                State new_state = current.getSt().simulateAction(action, walls, simulate_monster);

                if (new_state != null) {
                    int cost = current.getG() + 1;

                    Node children = new Node(new_state, cost, cost + manhattanDist(new_state.getPosition(), target), current, action);

                    if (!closed.contains(children)) {
                        open.put(children, children);
                    }
                }
            }
        }

        return null;
    }

    private LinkedList<ACTIONS> findPath(Node initial, Vector2d target)
    {
        return findPath(initial, target, false);
    }

    /**
     * @brief Scale pixel coordinates to grid coordinates.
     * @param pixel_coord pixel coordinates to scale.
     * @return grid_coord grid coordinates.
     */
    private Vector2d scale(Vector2d pixel_coord) {
        Vector2d grid_coord = new Vector2d();

        grid_coord.x = Math.floor(pixel_coord.x / fscale.x);
        grid_coord.y = Math.floor(pixel_coord.y / fscale.y);

        return grid_coord;
    }

    /**
     * @brief Calculate manhattanDist between two coordinates.
     * @param a Point a.
     * @param b Point b.
     * @return manhattanDist between a and b.
     */
    private int manhattanDist(Vector2d a, Vector2d b) {
        return (int) (abs(a.x - b.x) + abs(a.y - b.y));
    }

    /**
     * @brief Calculate cost associated to pick a gem first by an heuristic (Pick first gems close to avatar, last close to portal).
     * @param gem Grid coordinates of gem.
     * @param stateObs Observation of the current state.
     * @return Heuristic cost
     */
    private double gemHeuristicOrder(Vector2d gem, StateObservation stateObs) {
//        double dist_avatar = manhattanDist(gem, scale(stateObs.getAvatarPosition()));
//        double dist_portal = manhattanDist(gem, portal);

        double dist_avatar = dist_gems_avatar.get(new AbstractMap.SimpleEntry<>(gem.x, gem.y));
        double dist_portal = dist_gems_portal.get(new AbstractMap.SimpleEntry<>(gem.x, gem.y));

        return dist_avatar/dist_portal;
    }

    /**
     * @brief Calculate cost associated to pick a gem by an heuristic (Pick gems close to avatar and portal).
     * @param gem Grid coordinates of gem.
     * @param stateObs Observation of the current state.
     * @return Heuristic cost
     */
    private double gemHeuristicSelection(Vector2d gem, StateObservation stateObs) {
//        double dist_avatar = manhattanDist(gem, scale(stateObs.getAvatarPosition()));
//        double dist_portal = manhattanDist(gem, portal);

        double dist_avatar = dist_gems_avatar.get(new AbstractMap.SimpleEntry<>(gem.x, gem.y));
        double dist_portal = dist_gems_portal.get(new AbstractMap.SimpleEntry<>(gem.x, gem.y));

        return dist_avatar + dist_portal;
    }

    /**
     * @brief Get Orientation of avatar.
     * @param stateObs Observation of the current state.
     * @return int Orientation: 0 = Up, 1 = Right, 2 = Down, 3 = Left
     */
    private int getOrientation(StateObservation stateObs) {
        Vector2d obsOrientation = stateObs.getAvatarOrientation();
        int orientation = 0;

        if(obsOrientation.x == 0){
            if(obsOrientation.y == 1)
                orientation=2; // Down
            if(obsOrientation.y == -1)
                orientation=0; // Up
        }
        else{
            if(obsOrientation.x == 1)
                orientation=1; // Right
            if(obsOrientation.x == -1)
                orientation=3; // Left
        }

        return orientation;
    }

    /**
     * @brief Check if the avatar is in danger (a monster is close enough)
     * @param stateObs Observation of the current state.
     * @return danger
     */
    private Boolean checkDanger(StateObservation stateObs)
    {
        Boolean danger = false;

        if (stateObs.getNPCPositions() != null)
            for (Observation monster : stateObs.getNPCPositions()[0])
            {
                State monster_st = new State(scale(monster.position), 0);
                LinkedList<ACTIONS> path = findPath(new Node(monster_st, 0, manhattanDist(scale(stateObs.getAvatarPosition()), scale(monster.position))), scale(stateObs.getAvatarPosition()), true);

                if (path != null ) {
                    if ((!this.danger && path.size() <= enter_danger_threshold) || (this.danger && path.size() <= exit_danger_threshold)) {
                        danger = true;
                        break;
                    }
                }
            }

        this.danger = danger;
        return danger;
    }

    /**
     * @brief Get Hotness (calculated danger) of a grid position. Using precalculated hotnessmap and distances from monsters
     * @param casilla Grid coordinates.
     * @param stateObs Observation of the current state.
     * @return Hotness for casilla
     */
    private int getHotness(Vector2d casilla, StateObservation stateObs)
    {
        int hotness = hotnessmap[(int) casilla.x][(int) casilla.y];

        if (stateObs.getNPCPositions() != null)
            for (Observation monster: stateObs.getNPCPositions()[0])
            {
                State monster_st = new State(scale(monster.position), 0);
                LinkedList<ACTIONS> path = findPath(new Node(monster_st, 0, manhattanDist(casilla, monster_st.getPosition())), casilla, true);
                if (path != null)
                    hotness = Math.max(hotness, 8 - path.size());
            }

        return hotness;
    }
}