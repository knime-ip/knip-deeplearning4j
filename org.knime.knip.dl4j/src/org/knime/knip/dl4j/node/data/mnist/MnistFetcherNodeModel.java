package org.knime.knip.dl4j.node.data.mnist;

import java.util.ArrayList;
import java.util.List;

import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortType;
import org.knime.ext.dl4j.base.AbstractDLNodeModel;
import org.knime.knip.base.data.img.ImgPlusCell;
import org.knime.knip.base.data.img.ImgPlusCellFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;

import net.imagej.ImgPlus;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.numeric.real.FloatType;

/**
 * Node which downloads images from the MNIST dataset and supplies them at the output table.
 * Images are fetched using DL4J {@link MnistDataSetIterator}.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class MnistFetcherNodeModel extends AbstractDLNodeModel {

	// the logger instance
    private static final NodeLogger logger = NodeLogger
            .getLogger(MnistFetcherNodeModel.class);
	
    private SettingsModelString m_testOrTrain;
    private SettingsModelBoolean m_doBinarize;
    private SettingsModelIntegerBounded m_numberOfImages;   
    
	/**
     * Constructor for the node model.
     */
    protected MnistFetcherNodeModel() {   
    	super(new PortType[] {}, new PortType[] {BufferedDataTable.TYPE}); 
    }
	
    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
    	
    	String whatToFetch = m_testOrTrain.getStringValue();
    	boolean doBinarize = m_doBinarize.getBooleanValue();
    	
    	boolean testOrTrain;
    	if(whatToFetch.equals(new String("TRAIN"))){
    		testOrTrain = true;
    	} else if(whatToFetch.equals("TEST")){
    		testOrTrain = false;
    	} else {
    		throw new IllegalStateException("No case defined for this fetch type: " + whatToFetch);
    	}

    	int numberOfImages = m_numberOfImages.getIntValue();
    	BufferedDataContainer container = exec.createDataContainer(createOutputSpec());
    	final ImgPlusCellFactory imageCellFactory = new ImgPlusCellFactory(exec);
		final ArrayImgFactory<FloatType> floatImageFactory = new ArrayImgFactory<>();
    	
		MnistDataSetIterator mnistIter = new MnistDataSetIterator(1, numberOfImages, doBinarize, testOrTrain, false, 0);
		Double progress;
		
    	for(int i = 0; i < numberOfImages; i++){
    		DataSet imageLabelVector = mnistIter.next();
    		List<DataCell> cells = new ArrayList<>();
    		INDArray imageVector = imageLabelVector.getFeatureMatrix();
    		INDArray labelVector = imageLabelVector.getLabels();
    		
    		Img<FloatType> mnistImg = createImgFromINDArray(imageVector, floatImageFactory);
    		ImgPlusCell<FloatType> imgCell = imageCellFactory.createCell(new ImgPlus<FloatType>(mnistImg));
    		cells.add(imgCell);
    		
    		StringCell labelCell = new StringCell(createLabelFromINDArray(labelVector));
    		cells.add(labelCell);
    		
    		container.addRowToTable(new DefaultRow("Row" + i, cells));
    		
    		progress = new Double(i+1)/new Double(numberOfImages);
    		exec.setProgress(progress);
    	}
    
    	container.close();
    	BufferedDataTable outputTable = container.getTable();

    	return new BufferedDataTable[]{outputTable};
    }
    
    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
    	return new DataTableSpec[]{createOutputSpec()};
    }
    
	@Override
	protected List<SettingsModel> initSettingsModels() {
		m_testOrTrain = createTestOrTrainModel();
    	m_doBinarize = createDoBinarizeModel();
    	m_numberOfImages = createNumberOfImagesModel();   
		
		List<SettingsModel> settings = new ArrayList<>();
		settings.add(m_doBinarize);
		settings.add(m_numberOfImages);
		settings.add(m_testOrTrain);
		return settings;
	}
	
	public static SettingsModelString createTestOrTrainModel(){
		return new SettingsModelString("test_or_train", "TRAIN");
	}
	
	public static SettingsModelBoolean createDoBinarizeModel(){
		return new SettingsModelBoolean("do_binarize", false);
	}
	
	public static SettingsModelIntegerBounded createNumberOfImagesModel(){
		return new SettingsModelIntegerBounded("number_of_images_to_fetch", 1, 1, 60000);
	}
	
	private DataTableSpec createOutputSpec(){
		DataColumnSpec[] colSpecs = new DataColumnSpec[2];
		
		DataColumnSpecCreator listColSpecCreator = new DataColumnSpecCreator("mnist_images", DataType.getType(ImgPlusCell.class));
		colSpecs[0] = listColSpecCreator.createSpec();
		
		listColSpecCreator = new DataColumnSpecCreator("mnist_labels", DataType.getType(StringCell.class));
		colSpecs[1] = listColSpecCreator.createSpec();
		
    	return new DataTableSpec(colSpecs);
	}
    
	/**
	 * Creates a {@link Img} from a {@link INDArray}. 28 by 28 images are expected and that 
	 * the array contains the flattened image.
	 * 
	 * @param flattenedImage the array containing the image pixel values
	 * @param fac the factory to use for image creation
	 * @return the image containing the pixel values from the array
	 */
	private Img<FloatType> createImgFromINDArray(final INDArray flattenedImage, final ArrayImgFactory<FloatType> fac){
		Img<FloatType> img = fac.create(new long[]{28,28}, new FloatType());
		RandomAccess<FloatType> imgAccess = img.randomAccess();
		Cursor<FloatType> imgCursor = img.localizingCursor();
		
		//vector cursor
		int k = 0;
		while(imgCursor.hasNext()){
			imgCursor.fwd();
			imgAccess.setPosition(imgCursor);   			
			imgAccess.get().set(flattenedImage.getFloat(k));
			k++;
		}
		
		return img;
	}
	
	/**
	 * Converts from MNIST one-hot label vector, hence a vector of length 10 with one position with value 1.0 marking
	 * the number label of the corresponding MNIST image, to label.
	 * 
	 * @param labelVector the MNIST one-hot label vector
	 * @return corresponding label
	 */
	private String createLabelFromINDArray(final INDArray labelVector){
		float label = 0;
		for(int i = 0; i < labelVector.length(); i++){
			if(labelVector.getFloat(i) == 1.0){
				//index corresponds directly to the MNIST label
				label = i;
				break;
			}
		}
		return  (int)label + "";
	}
   
}

