package org.roda.rodain.schema.ui;

import java.util.*;

import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import org.roda.rodain.rules.Rule;
import org.roda.rodain.rules.sip.SipPreview;
import org.roda.rodain.schema.DescriptionObject;
import org.roda.rodain.utils.FontAwesomeImageCreator;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 17-09-2015.
 */
public class SchemaNode extends TreeItem<String>implements Observer {
  private DescriptionObject dob;
  private Map<String, Integer> rules;
  private Map<String, Rule> ruleObjects;
  private Map<String, Set<SipPreviewNode>> sips;
  private Image icon;

  private Set<SchemaNode> schemaNodes;

  /**
   * Creates a new SchemaNode
   * 
   * @param dobject
   *          The DescriptionObject that defines the SchemaNode
   */
  public SchemaNode(DescriptionObject dobject) {
    super(dobject.getTitle());
    dob = dobject;
    rules = new HashMap<>();
    sips = new HashMap<>();
    ruleObjects = new HashMap<>();
    schemaNodes = new HashSet<>();

    ResourceBundle hierarchyConfig = ResourceBundle.getBundle("properties/roda-description-levels-hierarchy");
    String category = hierarchyConfig.getString("category." + dobject.getDescriptionlevel());
    String unicode = hierarchyConfig.getString("icon." + category);

    Image im = FontAwesomeImageCreator.generate(unicode);
    icon = im;
    this.setGraphic(new ImageView(im));
  }

  /**
   * Updates the node when a Rule has been modified.
   * 
   * @param o
   *          The observable object
   * @param arg
   *          The arguments sent by the object
   */
  @Override
  public void update(final Observable o, Object arg) {
    if (o instanceof Rule) {
      final Rule rule = (Rule) o;
      final Integer idInt = rule.getId();
      final String id = idInt.toString();
      // set the title with the sip count
      int count = rule.getSipCount();
      rules.put(id, count);
      updateValue();

      Platform.runLater(new Runnable() {
        @Override
        public void run() {
          // replace sips from this rule
          if (sips.get(id) != null) {
            getChildren().removeAll(sips.get(id));
          }
          Set<SipPreviewNode> nodes = new HashSet<>(rule.getSipNodes());
          sips.put(id, nodes);
          getChildren().addAll(rule.getSipNodes());
          sortChildren();
        }
      });
    }
  }

  private void updateValue() {
    int sipCount = getSipCount();
    if (sipCount > 0)
      setValue(String.format("%s  (%d items)", dob.getTitle(), getSipCount()));
    else
      setValue(dob.getTitle());
  }

  /**
   * Adds a new Rule to the SchemaNode.
   *
   * @param r
   *          The Rule to be added
   */
  public void addRule(Rule r) {
    int count = r.getSipCount();
    Integer idInt = r.getId();
    String id = idInt.toString();
    // add the rule the maps
    rules.put(id, count);
    ruleObjects.put(id, r);
    // update the value of the TreeItem
    int sipCount = getSipCount();
    if (sipCount > 0)
      setValue(String.format("%s  (%d items)", dob.getTitle(), getSipCount()));
    setExpanded(true);
  }

  /**
   * Removes a rule from the SchemaNode.
   *
   * @param r
   */
  public void removeRule(Rule r) {
    Integer idInt = r.getId();
    String id = idInt.toString();
    if (sips.get(id) != null) {
      getChildren().removeAll(sips.get(id));
    }
    // remove the rules from the maps
    rules.remove(id);
    ruleObjects.remove(id);
    sips.remove(id);
    r.remove();
    // update the value of the TreeItem
    String text = dob.getTitle();
    int count = getSipCount();
    if (count > 0)
      text += String.format("  (%d items)", count);
    setValue(text);
  }

  public void addChildrenNode(SchemaNode node) {
    schemaNodes.add(node);
  }

  /**
   * Sorts the children of the SchemaNode
   * 
   * @see SchemaComparator
   */
  public void sortChildren() {
    ArrayList<TreeItem<String>> aux = new ArrayList<>(getChildren());
    Collections.sort(aux, new SchemaComparator());
    getChildren().setAll(aux);
  }

  /**
   * @return The count of all the SIPs associated to the SchemaNode
   */
  public int getSipCount() {
    int result = 0;
    for (int i : rules.values())
      result += i;
    return result;
  }

  /**
   * @return The icon of the SchemaNode
   */
  public Image getImage() {
    return icon;
  }

  /**
   * @return The DescriptionObject that defines the SchemaNode
   */
  public DescriptionObject getDob() {
    return dob;
  }

  /**
   * @return The Set of rules associated to the SchemaNode, ordered by Rule id
   */
  public Set<Rule> getRules() {
    return new TreeSet<>(ruleObjects.values());
  }

  /**
   * @return A map of all the SIPs in the SchemaNode and in the SchemaNode's
   *         children
   */
  public Map<SipPreview, String> getSipPreviews() {
    Map<SipPreview, String> result = new HashMap<>();
    // this node's sips
    for (Rule r : ruleObjects.values())
      for (SipPreview sp : r.getSips())
        result.put(sp, dob.getId());

    // children sips
    for (SchemaNode sn : schemaNodes)
      result.putAll(sn.getSipPreviews());
    return result;
  }
}