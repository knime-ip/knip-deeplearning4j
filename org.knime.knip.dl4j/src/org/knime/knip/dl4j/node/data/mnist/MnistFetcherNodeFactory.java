package org.knime.knip.dl4j.node.data.mnist;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "MnistFetcher" Node.
 * 
 *
 * @author KNIME
 */
public class MnistFetcherNodeFactory 
        extends NodeFactory<MnistFetcherNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public MnistFetcherNodeModel createNodeModel() {
        return new MnistFetcherNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<MnistFetcherNodeModel> createNodeView(final int viewIndex,
            final MnistFetcherNodeModel nodeModel) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new MnistFetcherNodeDialog();
    }

}

