package org.knime.knip.dl4j.data.convert;

import java.util.Iterator;

import org.knime.core.data.convert.java.DataCellToJavaConverter;
import org.knime.core.data.convert.java.DataCellToJavaConverterFactory;
import org.knime.knip.base.data.img.ImgPlusValue;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;

public class ImgPlusValueToINDArrayConverterFactory implements DataCellToJavaConverterFactory<ImgPlusValue, INDArray> {

	private class ImgPlusValueToINDArrayConverter<T extends RealType<T>> implements DataCellToJavaConverter<ImgPlusValue<T>, INDArray>{
		@Override
		public INDArray convert(ImgPlusValue<T> source) throws Exception {
			Img<T> img = source.getImgPlus();			
			Iterator<T> imgIter = img.iterator();
			INDArray flattenedImg = Nd4j.create((int)calcNumPixels(source.getDimensions()));
			int i = 0;
			while(imgIter.hasNext()){
				flattenedImg.putScalar(i, imgIter.next().getRealDouble());
				i++;
			}
			return flattenedImg;
		}	
		
		private long calcNumPixels(long[] dims){
			long numPix = 1;
			for(long dim : dims){
				numPix *= dim;
			}
			return numPix;
		}
	}
	
	@Override
	public DataCellToJavaConverter<ImgPlusValue, INDArray> create() {
		return new ImgPlusValueToINDArrayConverter();
	}

	@Override
	public Class<ImgPlusValue> getSourceType() {
		return ImgPlusValue.class;
	}

	@Override
	public Class<INDArray> getDestinationType() {
		return INDArray.class;
	}

	@Override
	public String getIdentifier() {
		return getClass().getName() + "(" + ImgPlusValue.class.getSimpleName() + "," + INDArray.class.toString() + "," + ""
	            + ")";
	}

	
}
