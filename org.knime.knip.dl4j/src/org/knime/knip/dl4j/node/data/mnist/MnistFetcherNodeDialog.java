package org.knime.knip.dl4j.node.data.mnist;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentNumberEdit;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;

/**
 * <code>NodeDialog</code> for the "MnistFetcher" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author David Kolb, KNIME.com GmbH
 */
public class MnistFetcherNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring the MnistFetcher node.
     */
    protected MnistFetcherNodeDialog() {
    	addDialogComponent(new DialogComponentBoolean(
    			MnistFetcherNodeModel.createDoBinarizeModel(), 
    			"Binarize Images?"));
    	addDialogComponent(new DialogComponentStringSelection(
    			MnistFetcherNodeModel.createTestOrTrainModel(),
				"What to Fetch",
				new String[]{"TEST","TRAIN"}
				));
    	addDialogComponent(new DialogComponentNumberEdit(
				MnistFetcherNodeModel.createNumberOfImagesModel(),
				"Number of Images to Fetch",
				5
				));
    }
}

