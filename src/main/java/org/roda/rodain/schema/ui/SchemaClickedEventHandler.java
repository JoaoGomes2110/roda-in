package org.roda.rodain.schema.ui;

import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;

import org.roda.rodain.core.Main;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 21-09-2015.
 */
public class SchemaClickedEventHandler implements EventHandler<MouseEvent> {
  private TreeView<String> treeView;

  public SchemaClickedEventHandler(SchemaPane pane) {
    this.treeView = pane.getTreeView();
  }

  @Override
  public void handle(MouseEvent mouseEvent) {
    if (mouseEvent.getClickCount() == 1) {
      TreeItem source = treeView.getSelectionModel().getSelectedItem();
      if (source instanceof SipPreviewNode) {
        Main.getInspectionPane().update((SipPreviewNode) source);
      }
      if (source instanceof SchemaNode) {
        Main.getInspectionPane().update((SchemaNode) source);
      }
    }
  }
}
