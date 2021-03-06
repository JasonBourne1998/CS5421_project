package graphical.editor.LinAlgTree;

import graphical.editor.Operator;

import java.util.ArrayList;

/***
 * The overall tree-like structure representing the graph. The graph can have excess leaf nodes (disconnected graphs)
 * but not excess parent nodes. Each parent node must have a valid pairing with the appropriate child nodes.
 */
public class MainTree {

    private ArrayList<ArrayList<Node>> layers;
    private int numLayers;

    public MainTree() {
        layers = new ArrayList<>();
        numLayers = 0;
    }

    /***
     * Adds a leaf node in the specified layer if possible, creating at most one additional layer if required.
     *
     * @param leaf The leaf node to be inserted into the tree.
     * @param level The level of the tree to insert the new leaf. Follows 0-indexing.
     */
    public void addLeaf(Node leaf, int level) throws Exception {
        if (!(leaf instanceof LeafNode)) {
            System.out.println("The specified node is not a leaf.");
            return;
        }
        if (level > numLayers) {
            System.out.println("Cannot add node to layers beyond the max level + 1.");
            return;
        }
        if (level == numLayers) {
            addLayer();
        }
        layers.get(level).add(leaf);
    }

    /***
     * Adds a non-leaf node to the specified layer if possible, creating at most one additional layer if required.
     *
     * The parent (non-leaf) node must be compatible with the immediate available nodes in the layer below.
     * If no such nodes are available, the user is notified and the process is aborted.
     *
     * @param parent The non-leaf node to be inserted into the tree.
     * @param level The level of the tree to insert the parent node. Follows 0-indexing.
     */
    public void addParent(Node parent, int level) throws Exception {
        if (parent instanceof LeafNode) {
            System.out.println("The specified node is a leaf, not a parent.");
            return;
        }

        if (level > numLayers) {
            System.out.println("Cannot add node to layers beyond the max level + 1.");
            return;
        }

        if (level == 0) {
            System.out.println("Cannot add parent to layer 0, only layer 1 or above.");
            return;
        }

        boolean isCreateLayer = false;
        if (level == numLayers) {
            isCreateLayer = true;
            addLayer();
        }

        boolean isBinary = parent instanceof BinaryOperation;
        int numNewChildren = isBinary ? 2 : 1;
        int numExistingChildren = countNumChildren(level - 1);

        int numLowerNodes = layers.get(level - 1).size();
        if (numLowerNodes - numExistingChildren < numNewChildren) {
            System.out.println("Cannot add parent due to insufficient children nodes.");
            return;
        }

        if (isBinary) {
            try {
                BinaryOperation parentNode = new BinaryOperation(parent.getOperator(),
                        layers.get(level - 1).get(numExistingChildren),
                        layers.get(level - 1).get(numExistingChildren + 1),
                        parent.getParamString(), parent.getParams());
                layers.get(level).add(parentNode);
            } catch (Exception e) {
                if (isCreateLayer) {
                    dropLayer();
                }
                System.out.print("Cannot add parent due to incompatible children nodes.");
            }
        } else {
            try {
                UnaryOperation parentNode = new UnaryOperation(parent.getOperator(),
                        layers.get(level - 1).get(numExistingChildren), parent.getParamString(), parent.getParams());
                layers.get(level).add(parentNode);
            } catch (Exception e) {
                if (isCreateLayer) {
                    dropLayer();
                }
                System.out.println("Cannot add parent due to incompatible children nodes.");
            }
        }
    }

    public void removeParent(int level) throws Exception {
        if (level >= numLayers || layers.get(level).isEmpty() || layers.get(level).get(layers.get(level).size() - 1) instanceof LeafNode) {
            throw new Exception(String.format("Cannot remove parent: No such parent found in level %d", level));
        }
        int numNodes = layers.get(level).size();
        if (isChild(level, numNodes - 1)) {
            throw new Exception("Cannot remove parent as it is a child of some other node.");
        }
        layers.get(level).remove(numNodes - 1);
    }

    public Node getNode(int level, int index) throws IndexOutOfBoundsException {
        return layers.get(level).get(index);
    }

    public void addLayer() throws Exception {
        if (!layers.isEmpty() && (layers.get(0).isEmpty() || layers.get(numLayers - 1).isEmpty())) {
            throw new Exception("Cannot add layer when there is already an empty layer.");
        }
        layers.add(new ArrayList<>());
        numLayers++;
    }

    public void dropLayer() {
        assert(layers.get(numLayers - 1).isEmpty());
        layers.remove(numLayers - 1);
        numLayers--;
    }

    public void addFirstLayer() throws Exception {
        if (!layers.isEmpty() && (layers.get(0).isEmpty() || layers.get(numLayers - 1).isEmpty())) {
            throw new Exception("Cannot add layer when there is already an empty layer.");
        }
        layers.add(0, new ArrayList<>());
        numLayers++;
    }

    private void dropFirstLayer() {
        assert(layers.get(0).isEmpty());
        layers.remove(0);
        numLayers--;
    }

    private int countNumChildren(int targetLevel) {
        if (targetLevel >= numLayers - 1 || layers.get(targetLevel + 1).isEmpty()) {
            return 0;
        }
        int numChildren = 0;

        ArrayList<Node> nodes = layers.get(targetLevel + 1);
        for (Node node : nodes) {
            if (node instanceof BinaryOperation) {
                numChildren += 2;
            } else if (node instanceof UnaryOperation) {
                numChildren++;
            }
            // ignore leaf nodes
        }
        return numChildren;
    }

    private boolean isChild(int layerNumber, int index) {
        if (layerNumber >= numLayers - 1 || layers.get(layerNumber + 1).isEmpty()) {
            return false;
        }
        int numChildren = countNumChildren(layerNumber);
        return numChildren > index;
    }

    private boolean isConnected() {
        if (numLayers == 0 || layers.get(numLayers - 1).size() != 1) {
            return false;
        }
        int numNodesTotal = layers.stream()
                .map(ArrayList::size)
                .reduce(0, Integer::sum);
        int numNodesConnected = layers.get(numLayers - 1).get(0).getNumDescendants();
        return numNodesConnected == numNodesTotal;
    }

    /**
     * Convert the graph into a string in prefix notation.
     * @return The string corresponding to the sequence of operations in prefix notation.
     * @throws Exception If the graph is disconnected.
     */
    public String convertToString() throws Exception {
        if (!isConnected()) {
            throw new Exception("Unable to convert to relational algebra string. Graph is not connected.");
        }
        return layers.get(numLayers - 1).get(0).toString();
    }

    public void reset() {
        layers = new ArrayList<>();
        numLayers = 0;
    }

    /***
     * Creates an instance of a leaf node.
     * @param relation The schema in the format [table name](attr1,attr2,...)
     * @throws Exception If the schema is invalid.
     */
    public static LeafNode createLeafNode(String relation) throws Exception {
        return new LeafNode(relation);
    }

    public static UnaryOperation createPiOperation(String argString, String[] args) {
        return new UnaryOperation(Operator.PI, argString, args);
    }

    public static UnaryOperation createRhoOperation(String argString, String[] args) {
        return new UnaryOperation(Operator.RHO, argString, args);
    }

    public static UnaryOperation createDeltaOperation() {
        return new UnaryOperation(Operator.DELTA, "", new String[0]);
    }

    public static UnaryOperation createSigmaOperation(String argString, String[] args) {
        return new UnaryOperation(Operator.SIGMA, argString, args);
    }

    public static UnaryOperation createGammaOperation(String argString, String[] args) {
        return new UnaryOperation(Operator.GAMMA, argString, args);
    }

    public static UnaryOperation createTauOperation(String argString, String[] args) {
        return new UnaryOperation(Operator.TAU, argString, args);
    }

    public static BinaryOperation createCrossOperation() {
        return new BinaryOperation(Operator.CROSS, "", new String[0]);
    }

    public static BinaryOperation createInnerJoinOperation(String argString, String[] args) {
        return new BinaryOperation(Operator.INNER_JOIN, argString, args);
    }

    public static BinaryOperation createUnionOperation() {
        return new BinaryOperation(Operator.UNION, "", new String[0]);
    }

    public static BinaryOperation createIntersectionOperation() {
        return new BinaryOperation(Operator.INTERSECTION, "", new String[0]);
    }

    public static BinaryOperation createDifferenceOperation() {
        return new BinaryOperation(Operator.DIFFERENCE, "", new String[0]);
    }
}
