package edu.dfci.cccb.mev.presets.dataset.fs;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Delegate;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j;
import edu.dfci.cccb.mev.dataset.domain.simple.ArrayListSelections;
import edu.dfci.cccb.mev.dataset.domain.simple.SimpleDimension;
import edu.dfci.cccb.mev.presets.contract.PresetDescriptor;
import edu.dfci.cccb.mev.presets.contract.exceptions.PresetException;
import edu.dfci.cccb.mev.presets.dal.TsvReader;
import edu.dfci.cccb.mev.presets.dal.TsvReaderMetaModel;
import edu.dfci.cccb.mev.presets.prototype.AbstractPresetDescriptor;

@Log4j
@ToString
@RequiredArgsConstructor
public class PresetDescriptorFlatFileAdaptor extends AbstractPresetDescriptor implements PresetDescriptor {

  private final @Delegate PresetDescriptor wrapped;
  
  public URL binaryFolderUrl() throws MalformedURLException{
    log.debug("dataRootUrl:"+wrapped.dataRootUrl ());
    log.debug("relativeDataPath:"+wrapped.relativeDataPath());
    URL url = new URL(wrapped.dataRootUrl (), "binary/"+wrapped.relativeDataPath ()+"/");
    log.debug ("URL: "+url);
    return url;
  }
  
  public URL binaryUrl() throws MalformedURLException{
    
    return new URL(binaryFolderUrl(), "datasetValues.matrix");
  }
  
  public URL rowListUrl() throws MalformedURLException{
    URL url = new URL(binaryFolderUrl(), "rows.tsv");
    log.debug ("Row URL: "+url);
    return url;    
  }
  
  public URL columnListUrl() throws MalformedURLException{
    URL url = new URL(binaryFolderUrl(),"columns.tsv");
    log.debug ("Column URL: "+url);
    return url;
  }
  
  public Map<String, Integer> getRowKeys() throws PresetException{
     try {
      return getKeyMap(this.rowListUrl ());
    } catch (MalformedURLException e) {
      throw new PresetException ("Error while reading row keys, descriptor="+wrapped, e);
    }
  }
  public Map<String, Integer> getColumnKeys() throws PresetException{
    try {
      Map<String, Integer> keyMap = getKeyMap(this.columnListUrl ());
      log.debug("getColumnKeys:keyMap:"+keyMap);
      return keyMap;
    } catch (MalformedURLException e) {
      throw new PresetException ("Error while reading column keys, descriptor="+wrapped, e);
    }
  }
  
  private Map<String, Integer> getKeyMap(URL url){
    TsvReader reader = new TsvReaderMetaModel ();
    reader.init (url);
    String[] keys = reader.getColumnNames ();
    Map<String, Integer> keyMap = new LinkedHashMap<String, Integer>(keys.length);
    for(int i=0;i<keys.length;i++){
      keyMap.put (keys[i], i);
    }
    return keyMap;
  }
}
