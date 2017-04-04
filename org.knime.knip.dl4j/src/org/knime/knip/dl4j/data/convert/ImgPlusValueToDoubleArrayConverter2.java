package org.knime.knip.dl4j.data.convert;

import org.knime.ext.dl4j.base.data.convert.extension.BaseDL4JConverter;
import org.knime.knip.base.data.img.ImgPlusValue;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

/**
 * DL4JConverter that converts a ImgPlusValue to float[]. *
 * 
 * @author David Kolb, KNIME.com GmbH
 * 
 * @param
 */
@SuppressWarnings("rawtypes")
public class ImgPlusValueToDoubleArrayConverter2<T extends RealType<T>>
		extends BaseDL4JConverter<ImgPlusValue, double[]> {
	/** * Constructor for class ImgPlusValueToDoubleArrayConverter. */
	public ImgPlusValueToDoubleArrayConverter2() {
		super(ImgPlusValue.class, double[].class, BaseDL4JConverter.DEFAULT_PRIORITY + 1);
	}

	/**
	 * * {@inheritDoc} Flattens the image and copies the pixel values to
	 * double[].
	 */
	@Override
	@SuppressWarnings("unchecked")
	public double[] convert(ImgPlusValue sourceImg) throws Exception {

		long[] dims = sourceImg.getDimensions();

		final long[] reshapedDims;
		if (dims.length == 1) {
			reshapedDims = new long[] { 1, 1, dims[0] };
		} else if (dims.length == 2) {
			reshapedDims = new long[] { 1, dims[1], dims[0] };
		} else if (dims.length == 3 && dims[2] <= 4) {
			reshapedDims = new long[] { dims[2], dims[1], dims[0] };
		} else {
			throw new UnsupportedOperationException(
					"Can only process one or two dimensional images with maximum of four channels.");
		}

		final Img img = sourceImg.getImgPlus();
		final RandomAccess<T> source = (RandomAccess<T>) img.randomAccess();

		final ArrayImg<DoubleType, DoubleArray> dest = ArrayImgs.doubles(reshapedDims);
		final Cursor<DoubleType> destCursor = dest.cursor();
		for (int channel = 0; channel < dims[2]; channel++) {
			source.setPosition(channel, 2);
			for (int y = 0; y < dims[1]; y++) {
				source.setPosition(y, 1);
				for (int x = 0; x < dims[0]; x++) {
					source.setPosition(x, 0);
					destCursor.fwd();
					destCursor.get().set(source.get().getRealFloat());
				}
			}
		}
		return dest.update(null).getCurrentStorageArray();
	}
}