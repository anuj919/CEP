/* Generated By:JavaCC: Do not edit this line. ConfigFileParser.java */
package testdatagenerator.parser;

import java.io.*;
import event.*;
import event.eventtype.*;
import java.util.*;
import testdatagenerator.*;

public class ConfigFileParser implements ConfigFileParserConstants {

/**  
  A String based constructor for ease of use.
  **/
    public ConfigFileParser(String s)
    {
        this((Reader)(new StringReader(s)));

    }

    public List<RandomEventConfiguration> getEventConfig() throws ParseException {
        return eventDefs();
    }

  static final public List<RandomEventConfiguration> eventDefs() throws ParseException {
        List<RandomEventConfiguration> list = new ArrayList<RandomEventConfiguration>();
        RandomEventConfiguration config;
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case VALID_NAME:
        ;
        break;
      default:
        jj_la1[0] = jj_gen;
        break label_1;
      }
      config = eventDef();
                  list.add(config);
    }
    jj_consume_token(0);
         {if (true) return list;}
    throw new Error("Missing return statement in function");
  }

  static final public RandomEventConfiguration eventDef() throws ParseException {
        EventClass ec; RandomAttributeConfiguration attrConfig; Token t,rateToken=null; String className;
        PrimaryEventType eType = new PrimaryEventType();
        ArrayList<RandomAttributeConfiguration> attrConfigList = new ArrayList<RandomAttributeConfiguration>();
        Map<String,RandomAttributeConfiguration> map = new HashMap<String,RandomAttributeConfiguration>();
    t = jj_consume_token(VALID_NAME);
          className = t.image;
    jj_consume_token(LBRACE);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case VALID_NAME:
      attrConfig = attributeConfig(map);
                        attrConfigList.add(attrConfig);
                        eType.addAttribute(attrConfig.attrName, attrConfig.attrType);
      label_2:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case COMMA:
          ;
          break;
        default:
          jj_la1[1] = jj_gen;
          break label_2;
        }
        jj_consume_token(COMMA);
        attrConfig = attributeConfig(map);
                                attrConfigList.add(attrConfig);
                                eType.addAttribute(attrConfig.attrName, attrConfig.attrType);
      }
      break;
    default:
      jj_la1[2] = jj_gen;
      ;
    }
    jj_consume_token(RBRACE);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case DOUBLE_VAL:
      rateToken = jj_consume_token(DOUBLE_VAL);
      break;
    default:
      jj_la1[3] = jj_gen;
      ;
    }
                ec = new EventClass(className, eType);
                RandomEventConfiguration eventConfig = new RandomEventConfiguration();
                eventConfig.eClass = ec;
                eventConfig.map = map;
                eventConfig.time = 0;
                if(rateToken==null)
                        eventConfig.relativeRate=1.0;
                else
                        eventConfig.relativeRate=Double.parseDouble(rateToken.image);
                {if (true) return eventConfig;}
    throw new Error("Missing return statement in function");
  }

  static final public RandomAttributeConfiguration attributeConfig(Map<String,RandomAttributeConfiguration> map) throws ParseException {
        DistributionConfiguration distConfig;
        Token attrNameToken, attrTypeToken ; String[] strings = null;
        RandomAttributeConfiguration attrConfig = new RandomAttributeConfiguration();
    attrNameToken = jj_consume_token(VALID_NAME);
    jj_consume_token(COLON);
    attrTypeToken = jj_consume_token(VALID_NAME);
    jj_consume_token(COLON);
    distConfig = distConfig();
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case COLON:
      jj_consume_token(COLON);
      jj_consume_token(LPARAN);
      strings = stringValues();
      jj_consume_token(RPARAN);
      break;
    default:
      jj_la1[4] = jj_gen;
      ;
    }
                 attrConfig.attrName = attrNameToken.image;
                attrConfig.attrType = AttributeType.valueOf(attrTypeToken.image);
                attrConfig.distConfig = distConfig;
                attrConfig.strValues = strings;
                map.put(attrNameToken.image,attrConfig);
                {if (true) return attrConfig;}
    throw new Error("Missing return statement in function");
  }

  static final public DistributionConfiguration distConfig() throws ParseException {
        DistributionConfiguration distConfig=new DistributionConfiguration();
        Token distType, doubleToken; ArrayList<Double> list=new ArrayList<Double>();
    distType = jj_consume_token(VALID_NAME);
          distConfig.distType=DistributionConfiguration.DistributionType.valueOf(distType.image);
    jj_consume_token(LPARAN);
    doubleToken = jj_consume_token(DOUBLE_VAL);
          list.add(Double.parseDouble(doubleToken.image));
    label_3:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case COMMA:
        ;
        break;
      default:
        jj_la1[5] = jj_gen;
        break label_3;
      }
      jj_consume_token(COMMA);
      doubleToken = jj_consume_token(DOUBLE_VAL);
                                             list.add(Double.parseDouble(doubleToken.image));
    }
    jj_consume_token(RPARAN);
         distConfig.setDistribution(DistributionConfiguration.DistributionType.valueOf(distType.image),
                list.toArray(new Double[0]));  {if (true) return distConfig;}
    throw new Error("Missing return statement in function");
  }

  static final public String[] stringValues() throws ParseException {
        ArrayList<String> strings = new ArrayList<String>();
        Token strToken;
    strToken = jj_consume_token(QUOTED_STRING);
          strings.add(strToken.image);
    label_4:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case COMMA:
        ;
        break;
      default:
        jj_la1[6] = jj_gen;
        break label_4;
      }
      jj_consume_token(COMMA);
      strToken = jj_consume_token(QUOTED_STRING);
                 strings.add(strToken.image);
    }
          {if (true) return  strings.toArray(new String[0]);}
    throw new Error("Missing return statement in function");
  }

  static private boolean jj_initialized_once = false;
  static public ConfigFileParserTokenManager token_source;
  static SimpleCharStream jj_input_stream;
  static public Token token, jj_nt;
  static private int jj_ntk;
  static private int jj_gen;
  static final private int[] jj_la1 = new int[7];
  static private int[] jj_la1_0;
  static {
      jj_la1_0();
   }
   private static void jj_la1_0() {
      jj_la1_0 = new int[] {0x2000,0x20,0x2000,0x1000,0x400,0x20,0x20,};
   }

  public ConfigFileParser(java.io.InputStream stream) {
     this(stream, null);
  }
  public ConfigFileParser(java.io.InputStream stream, String encoding) {
    if (jj_initialized_once) {
      System.out.println("ERROR: Second call to constructor of static parser.  You must");
      System.out.println("       either use ReInit() or set the JavaCC option STATIC to false");
      System.out.println("       during parser generation.");
      throw new Error();
    }
    jj_initialized_once = true;
    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new ConfigFileParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 7; i++) jj_la1[i] = -1;
  }

  static public void ReInit(java.io.InputStream stream) {
     ReInit(stream, null);
  }
  static public void ReInit(java.io.InputStream stream, String encoding) {
    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 7; i++) jj_la1[i] = -1;
  }

  public ConfigFileParser(java.io.Reader stream) {
    if (jj_initialized_once) {
      System.out.println("ERROR: Second call to constructor of static parser.  You must");
      System.out.println("       either use ReInit() or set the JavaCC option STATIC to false");
      System.out.println("       during parser generation.");
      throw new Error();
    }
    jj_initialized_once = true;
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new ConfigFileParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 7; i++) jj_la1[i] = -1;
  }

  static public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 7; i++) jj_la1[i] = -1;
  }

  public ConfigFileParser(ConfigFileParserTokenManager tm) {
    if (jj_initialized_once) {
      System.out.println("ERROR: Second call to constructor of static parser.  You must");
      System.out.println("       either use ReInit() or set the JavaCC option STATIC to false");
      System.out.println("       during parser generation.");
      throw new Error();
    }
    jj_initialized_once = true;
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 7; i++) jj_la1[i] = -1;
  }

  public void ReInit(ConfigFileParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 7; i++) jj_la1[i] = -1;
  }

  static final private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }

  static final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

  static final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  static final private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  static private java.util.Vector jj_expentries = new java.util.Vector();
  static private int[] jj_expentry;
  static private int jj_kind = -1;

  static public ParseException generateParseException() {
    jj_expentries.removeAllElements();
    boolean[] la1tokens = new boolean[14];
    for (int i = 0; i < 14; i++) {
      la1tokens[i] = false;
    }
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 7; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 14; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.addElement(jj_expentry);
      }
    }
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = (int[])jj_expentries.elementAt(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  static final public void enable_tracing() {
  }

  static final public void disable_tracing() {
  }

}
