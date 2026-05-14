package work.lclpnet.activity.util;

import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DAG<T> {

    private final Map<Object, Node<T>> nodes = new HashMap<>();

    public Node<T> getOrCreateNode(Object key, T obj) {
        return nodes.computeIfAbsent(key, k -> new Node<>(key, obj));
    }

    public Optional<Node<T>> getNode(Object key) {
        return Optional.ofNullable(nodes.get(key));
    }

    public void removeNode(Object key) {
        var node = nodes.remove(key);
        if (node != null) node.detach();
    }

    public List<Node<T>> getTopologicalOrder() {
        return getTopologicalOrder(null);
    }

    public List<Node<T>> getTopologicalOrder(@Nullable Set<Node<T>> rootNodes) {
        // create a (partial) graph copy, so that this graph remains unmodified
        var graph = this.copy(rootNodes != null ? rootNodes::contains : v -> true);

        // Kahn's algorithm
        // https://en.wikipedia.org/w/index.php?title=Topological_sorting&oldid=1123299686#Kahn's_algorithm
        final List<Node<T>> L = new ArrayList<>();
        final Set<Node<T>> S = graph.nodes.values()
                .stream()
                .filter(Node::isRoot)
                .collect(Collectors.toSet());

        while (!S.isEmpty()) {
            var n = S.iterator().next();
            S.remove(n);
            L.add(n);

            for (var m : n.getChildren()) {
                m.getParents().remove(n);

                if (m.isRoot()) {
                    S.add(m);
                }
            }
        }

        // validate
        if (!L.stream().allMatch(Node::isRoot)) {
            throw new IllegalStateException("Cyclic dependency graph");
        }

        return L;
    }

    public DAG<T> copy(Predicate<Node<T>> filter) {
        var copy = new DAG<T>();

        this.nodes.forEach((k, v) -> {
            if (!filter.test(v)) return;

            Set<Node<T>> remain = new HashSet<>();
            remain.add(v);

            while (!remain.isEmpty()) {
                var oldNode = remain.iterator().next();
                remain.remove(oldNode);

                var node = copy.getOrCreateNode(oldNode.getKey(), oldNode.getObj());

                oldNode.getChildren().forEach(c -> {
                    var childNode = copy.getOrCreateNode(c.getKey(), c.getObj());
                    node.addChild(childNode);

                    remain.add(c);
                });
            }
        });

        return copy;
    }

    public static class Node<T> {

        private final Set<Node<T>> parents = new HashSet<>();
        private final Set<Node<T>> children = new HashSet<>();
        private final Object key;
        private final T obj;

        public Node(Object key, T obj) {
            this.key = key;
            this.obj = obj;
        }

        public Object getKey() {
            return key;
        }

        public T getObj() {
            return obj;
        }

        public boolean isRoot() {
            return parents.isEmpty();
        }

        public boolean addChild(Node<T> node) {
            if (node.hasChildDeep(this)) return false;

            children.add(node);
            node.parents.add(this);

            return true;
        }

        public void detach() {
            this.children.forEach(c -> c.parents.remove(this));
            this.parents.forEach(p -> p.children.remove(this));
        }

        public boolean hasChildDeep(Node<T> node) {
            for (var child : children) {
                if (node.equals(child) || child.hasChildDeep(node)) {
                    return true;
                }
            }

            return false;
        }

        public Set<Node<T>> getChildren() {
            return children;
        }

        public Set<Node<T>> getParents() {
            return parents;
        }

        @Override
        public String toString() {
            return "%s: (p=%s|c=%s)".formatted(
                    obj,
                    this.parents.stream().map(p -> p.obj.toString()).collect(Collectors.joining(",")),
                    this.children.stream().map(p -> p.obj.toString()).collect(Collectors.joining(","))
            );
        }
    }
}
