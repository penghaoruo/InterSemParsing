package brainllc.penghaoruo.isp;

import java.util.ArrayList;
import java.util.List;

public class QueryTree {
    public static class QueryNode {
        public PredArg data;
        public QueryNode parent;
        public List<QueryNode> children;
        
        public QueryNode() {
        	data = new PredArg();
        	data.pred = null;
        	data.args = null;
            parent = null;
            children = new ArrayList<QueryNode>();
        }
    }
    
    public QueryNode root;
    
    public QueryTree() {
        root = new QueryNode();
    }

    public QueryTree(PredArg rootData) {
        root = new QueryNode();
        root.data = rootData;
        root.children = new ArrayList<QueryNode>();
    }
    
    public String getStringRep() {
    	String res = "";
    	for (QueryNode node : root.children) {
    		res += getLevelRep(node, 0, "");
    	}
    	return res;
    }

	private String getLevelRep(QueryNode node, int level, String res) {
		String tab = "";
		for (int i = 0; i < level; i++) {
			tab += "\t";
		}
		res += tab + getNodeRep(node) + "\n";
		for (QueryNode child : node.children) {
			res = getLevelRep(child, level+1, res);
		}
		return res;
	}

	private String getNodeRep(QueryNode node) {
		String res = "";
		res = ParseUtils.augmentVerb(node.data.pred);
		for (String arg : node.data.args) {
			res += "  " + arg;
		}
		return res;
	}
}
