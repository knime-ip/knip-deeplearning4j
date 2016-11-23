package org.knime.knip.dl4j.data.convert;

import java.util.Iterator;

import org.knime.ext.dl4j.base.data.convert.extension.BaseDL4JConverter;
import org.knime.knip.base.data.img.ImgPlusValue;

import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;

/**
 * DL4JConverter that converts a ImgPlusValue to double[].
 * 
 * @author David Kolb, KNIME.com GmbH
 *
 * @param <T>
 */
@SuppressWarnings("rawtypes")
public class ImgPlusValueToDoubleArrayConverter<T extends RealType<T>>
		extends BaseDL4JConverter<ImgPlusValue, double[]> {

	/**
	 * Constructor for class ImgPlusValueToDoubleArrayConverter.
	 */
	public ImgPlusValueToDoubleArrayConverter() {
		super(ImgPlusValue.class, double[].class, BaseDL4JConverter.DEFAULT_PRIORITY);
	}

	/**
	 * {@inheritDoc} Flattens the image and copies the pixel values to double[].
	 */
	@Override
	@SuppressWarnings("unchecked")
	public double[] convert(ImgPlusValue source) throws Exception {
		Img<T> img = source.getImgPlus();
		Iterator<T> imgIter = img.iterator();
		double[] flattenedImg = new double[(int) calcNumPixels(source.getDimensions())];
		int i = 0;
		while (imgIter.hasNext()) {
			flattenedImg[i] = imgIter.next().getRealDouble();
			i++;
		}
		return flattenedImg;
	}

	private long calcNumPixels(long[] dims) {
		long numPix = 1;
		for (long dim : dims) {
			numPix *= dim;
		}
		return numPix;
	}

}
