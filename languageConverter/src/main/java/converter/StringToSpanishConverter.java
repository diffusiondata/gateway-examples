package converter;

import com.diffusiondata.gateway.framework.converters.PayloadConverter;
import com.diffusiondata.gateway.framework.exceptions.PayloadConversionException;

import converter.model.Spanish;

/**
 * Sample converter for testing.
 *
 * @author ndhougoda-hamal
 * @since 2.0
 */
public final class StringToSpanishConverter
    implements PayloadConverter<String, Spanish> {

    @Override
    public Spanish convert(String input) throws PayloadConversionException {
        return new Spanish(input);
    }
}
