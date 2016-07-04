/*******************************************************************************
 * Copyright by KNIME GmbH, Konstanz, Germany
 * Website: http://www.knime.org; Email: contact@knime.org
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, Version 3, as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7:
 *
 * KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 * Hence, KNIME and ECLIPSE are both independent programs and are not
 * derived from each other. Should, however, the interpretation of the
 * GNU GPL Version 3 ("License") under any applicable laws result in
 * KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 * you the additional permission to use and propagate KNIME together with
 * ECLIPSE with only the license terms in place for ECLIPSE applying to
 * ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 * license terms of ECLIPSE themselves allow for the respective use and
 * propagation of ECLIPSE together with KNIME.
 *
 * Additional permission relating to nodes for KNIME that extend the Node
 * Extension (and in particular that are based on subclasses of NodeModel,
 * NodeDialog, and NodeView) and that only interoperate with KNIME through
 * standard APIs ("Nodes"):
 * Nodes are deemed to be separate and independent programs and to not be
 * covered works.  Notwithstanding anything to the contrary in the
 * License, the License does not apply to Nodes, you are not required to
 * license Nodes under the License, and you are granted a license to
 * prepare and propagate Nodes, in each case even if such Nodes are
 * propagated with or for interoperation with KNIME.  The owner of a Node
 * may freely choose the license terms applicable to such Node, including
 * when such Node is propagated with or for interoperation with KNIME.
 *******************************************************************************/
package org.knime.knip.dl4j.data.convert;

import java.util.Iterator;

import org.knime.core.data.convert.java.DataCellToJavaConverter;
import org.knime.core.data.convert.java.DataCellToJavaConverterFactory;
import org.knime.knip.base.data.img.ImgPlusValue;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;

/**
 * Implementation of {@link DataCellToJavaConverterFactory} creating a converter from
 * ImgPlusValue to INDArray.
 * 
 * @author David Kolb, KNIME.com GmbH
 */
public class ImgPlusValueToINDArrayConverterFactory implements DataCellToJavaConverterFactory<ImgPlusValue, INDArray> {

	/**
	 * Implementation of {@link DataCellToJavaConverter} which converts from ImgPlusValue to
	 * INDArray. Images are converted by iterating the image and copying each pixel, hence images
	 * will be just flattened.
	 *
	 * @author David Kolb, KNIME.com GmbH
	 * @param <T>
	 */
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
