package de.bushnaq.abdalla.profiler;

/**
 * Categorises a code block to mainly spending time on one of the following activities.
 *
 * @author abdalla
 */
public enum SampleType {
    CPU,//mainly cpu intense code, e.g. searching or number crunching (very fast)
    FILE,//mainly file access intense code (very slow)
    GPU,//mainly drawing images 2D or 3D
    OTHR,//all the rest (should be ~1%),//mainly accessing network, but not using rest api or smb
    REST,//rest api calls or serving
    SMB,//access files using smb protocol
    SQL,//accessign a db using sql
    TCP
}
