package org.titiplex.io;

import org.titiplex.model.RawBlock;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface BlockReader {
    List<RawBlock> read(InputStream inputStream) throws IOException;
}
