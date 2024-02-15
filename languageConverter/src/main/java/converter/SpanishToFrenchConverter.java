package converter;

import com.diffusiondata.gateway.framework.converters.PayloadConverter;
import com.diffusiondata.gateway.framework.exceptions.PayloadConversionException;

import converter.model.French;
import converter.model.Spanish;

/**
 * Sample converter for testing.
 *
 * @author ndhougoda-hamal
 * @since 2.0
 */
public final class SpanishToFrenchConverter
    implements PayloadConverter<Spanish, French> {

    @Override
    public French convert(Spanish input) throws PayloadConversionException {
        return new French(input);
    }
}
