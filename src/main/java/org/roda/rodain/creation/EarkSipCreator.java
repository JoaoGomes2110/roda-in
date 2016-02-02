package org.roda.rodain.creation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.roda.rodain.core.AppProperties;
import org.roda.rodain.rules.TreeNode;
import org.roda.rodain.rules.sip.SipPreview;
import org.roda_project.commons_ip.model.SIP;
import org.roda_project.commons_ip.model.SIPDescriptiveMetadata;
import org.roda_project.commons_ip.model.SIPRepresentation;
import org.roda_project.commons_ip.model.impl.eark.EARKSIP;
import org.roda_project.commons_ip.utils.EARKEnums;
import org.roda_project.commons_ip.utils.METSEnums;
import org.roda_project.commons_ip.utils.SIPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 19/11/2015.
 */
public class EarkSipCreator extends SimpleSipCreator {
  private static final Logger log = LoggerFactory.getLogger(EarkSipCreator.class.getName());

  /**
   * Creates a new EARK SIP exporter.
   *
   * @param outputPath
   *          The path to the output folder of the SIP exportation
   * @param previews
   *          The map with the SIPs that will be exported
   */
  public EarkSipCreator(Path outputPath, Map<SipPreview, String> previews) {
    super(outputPath, previews);
  }

  /**
   * Attempts to create an EARK SIP of each SipPreview
   */
  @Override
  public void run() {
    for (SipPreview preview : previews.keySet()) {
      if (canceled) {
        break;
      }
      createEarkSip(previews.get(preview), preview);
    }
  }

  private void createEarkSip(String schemaId, SipPreview sip) {
    Path rodainPath = AppProperties.rodainPath;
    String metadataName = "metadata.xml";
    try {
      SIP earkSip = new EARKSIP(sip.getId(), EARKEnums.ContentType.mixed, "RODA-In");
      earkSip.setParent(schemaId);
      SIPRepresentation rep = new SIPRepresentation("rep1");

      currentSipName = sip.getName();
      currentAction = actionCopyingMetadata;
      String templateType = sip.getTemplateType();
      METSEnums.MetadataType metadataType = METSEnums.MetadataType.OTHER;

      if (templateType != null) {
        if (templateType.equals("dc")) {
          metadataName = "dc.xml";
          metadataType = METSEnums.MetadataType.DC;
        } else if (templateType.equals("ead")) {
          metadataName = "ead.xml";
          metadataType = METSEnums.MetadataType.EAD;
        } else {
          metadataName = "custom.xml";
          metadataType = METSEnums.MetadataType.OTHER;
        }
      }

      String content = sip.getMetadataContent();

      FileUtils.writeStringToFile(rodainPath.resolve(metadataName).toFile(), content);
      SIPDescriptiveMetadata metadata = new SIPDescriptiveMetadata(rodainPath.resolve(metadataName), null, metadataType,
        sip.getMetadataVersion());
      earkSip.addDescriptiveMetadata(metadata);

      currentAction = actionCopyingData;
      for (TreeNode tn : sip.getFiles()) {
        addFileToRepresentation(tn, new ArrayList<>(), rep);
      }

      earkSip.addRepresentation(rep);

      earkSip.build(outputPath);
      currentAction = actionFinalizingSip;
      createdSipsCount++;
    } catch (SIPException e) {
      log.error("Commons IP exception", e);
      unsuccessful.add(sip);
    } catch (IOException e) {
      log.error("Error accessing the files", e);
      unsuccessful.add(sip);
    }
  }

  private void addFileToRepresentation(TreeNode tn, List<String> relativePath, SIPRepresentation rep) {
    if (Files.isDirectory(tn.getPath())) {
      // add this directory to the path list
      List<String> newRelativePath = new ArrayList<>(relativePath);
      newRelativePath.add(tn.getPath().getFileName().toString());
      // recursive call to all the node's children
      for (TreeNode node : tn.getAllFiles().values()) {
        addFileToRepresentation(node, newRelativePath, rep);
      }
    } else {
      // if it's a file, add it to the representation
      rep.addData(tn.getPath(), relativePath);
    }
  }
}
