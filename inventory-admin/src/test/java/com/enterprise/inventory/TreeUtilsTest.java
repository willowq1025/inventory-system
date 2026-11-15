package com.enterprise.inventory;

import com.enterprise.common.core.util.TreeUtils;
import lombok.Data;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TreeUtilsTest {

    @Data
    static class Node {
        private Long id;
        private Long parentId;
        private String name;
        private List<Node> children;

        Node(Long id, Long parentId, String name) {
            this.id = id;
            this.parentId = parentId;
            this.name = name;
        }
    }

    @Test
    @DisplayName("通用树构建：父子关系与子孙收集")
    void buildTree() {
        List<Node> nodes = List.of(
                new Node(1L, 0L, "根"),
                new Node(2L, 1L, "子1"),
                new Node(3L, 1L, "子2"),
                new Node(4L, 2L, "孙1"));

        List<Node> roots = TreeUtils.build(nodes, Node::getId, Node::getParentId, Node::setChildren, 0L);
        assertThat(roots).hasSize(1);
        assertThat(roots.get(0).getChildren()).hasSize(2);
        assertThat(roots.get(0).getChildren().get(0).getChildren()).hasSize(1);

        List<Long> descendants = TreeUtils.collectDescendants(nodes, 1L, Node::getId, Node::getParentId);
        assertThat(descendants).containsExactlyInAnyOrder(1L, 2L, 3L, 4L);
    }
}
