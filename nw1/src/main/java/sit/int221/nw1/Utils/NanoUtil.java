package sit.int221.nw1.Utils;

import org.springframework.stereotype.Component;

import java.util.UUID;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;

@Component
public class NanoUtil {
    public String nanoIdGenerate(Integer size) {
        return NanoIdUtils.randomNanoId(NanoIdUtils.DEFAULT_NUMBER_GENERATOR, NanoIdUtils.DEFAULT_ALPHABET, size);
    }
}

