package com.wordnik.swagger.codegen.languages;

import com.wordnik.swagger.codegen.*;
import com.wordnik.swagger.util.Json;
import com.wordnik.swagger.models.properties.*;

import java.util.*;
import java.io.File;

public class Qt5CPPGenerator extends DefaultCodegen implements CodegenConfig {
  protected Set<String> foundationClasses = new HashSet<String>();

  // source folder where to write the files
  protected String sourceFolder = "client";
  protected String apiVersion = "1.0.0";
  protected final String PREFIX = "SWG";
  protected Map<String, String> namespaces = new HashMap<String, String>();
  protected Set<String> systemIncludes = new HashSet<String>();

  /**
   * Configures the type of generator.
   * 
   * @return  the CodegenType for this generator
   * @see     com.wordnik.swagger.codegen.CodegenType
   */
  public CodegenType getTag() {
    return CodegenType.CLIENT;
  }

  /**
   * Configures a friendly name for the generator.  This will be used by the generator
   * to select the library with the -l flag.
   * 
   * @return the friendly name for the generator
   */
  public String getName() {
    return "qt5cpp";
  }

  /**
   * Returns human-friendly help for the generator.  Provide the consumer with help
   * tips, parameters here
   * 
   * @return A string value for the help message
   */
  public String getHelp() {
    return "Generates a qt5 C++ client library.";
  }

  public Qt5CPPGenerator() {
    super();

    // set the output folder here
    outputFolder = "generated-code/qt5cpp";

    /**
     * Models.  You can write model files using the modelTemplateFiles map.
     * if you want to create one template for file, you can do so here.
     * for multiple files for model, just put another entry in the `modelTemplateFiles` with
     * a different extension
     */
    modelTemplateFiles.put(
      "model-header.mustache",
      ".h");

    modelTemplateFiles.put(
      "model-body.mustache",
      ".cpp");

    /**
     * Api classes.  You can write classes for each Api file with the apiTemplateFiles map.
     * as with models, add multiple entries with different extensions for multiple files per
     * class
     */
    apiTemplateFiles.put(
      "api-header.mustache",   // the template to use
      ".h");       // the extension for each file to write

    apiTemplateFiles.put(
      "api-body.mustache",   // the template to use
      ".cpp");       // the extension for each file to write

    /**
     * Template Location.  This is the location which templates will be read from.  The generator
     * will use the resource stream to attempt to read the templates.
     */
    templateDir = "qt5cpp";

    /**
     * Reserved words.  Override this with reserved words specific to your language
     */
    reservedWords = new HashSet<String> (
      Arrays.asList(
        "sample1",  // replace with static values
        "sample2")
    );

    /**
     * Additional Properties.  These values can be passed to the templates and
     * are available in models, apis, and supporting files
     */
    additionalProperties.put("apiVersion", apiVersion);
    additionalProperties().put("prefix", PREFIX);

    /**
     * Language Specific Primitives.  These types will not trigger imports by
     * the client generator
     */
    languageSpecificPrimitives = new HashSet<String>(
      Arrays.asList(
        "bool",
        "qint32",
        "qint64")
    );

    supportingFiles.add(new SupportingFile("helpers-header.mustache", sourceFolder, PREFIX + "Helpers.h"));
    supportingFiles.add(new SupportingFile("helpers-body.mustache", sourceFolder, PREFIX + "Helpers.cpp"));
    supportingFiles.add(new SupportingFile("HttpRequest.h", sourceFolder, PREFIX + "HttpRequest.h"));
    supportingFiles.add(new SupportingFile("HttpRequest.cpp", sourceFolder, PREFIX + "HttpRequest.cpp"));
    supportingFiles.add(new SupportingFile("modelFactory.mustache", sourceFolder, PREFIX + "ModelFactory.h"));
    supportingFiles.add(new SupportingFile("object.mustache", sourceFolder, PREFIX + "Object.h"));

    super.typeMapping = new HashMap<String, String>();

    typeMapping.put("Date", "QDate");
    typeMapping.put("DateTime", "QDateTime");
    typeMapping.put("string", "QString");
    typeMapping.put("integer", "qint32");
    typeMapping.put("long", "qint64");
    typeMapping.put("boolean", "bool");
    typeMapping.put("array", "QList");
    typeMapping.put("map", "QMap");
    // typeMapping.put("number", "Long");
    typeMapping.put("object", PREFIX + "Object");

    importMapping = new HashMap<String, String>();

    namespaces = new HashMap<String, String> ();

    foundationClasses.add("QString");

    systemIncludes.add("QString");
    systemIncludes.add("QList");
  }

  @Override
  public String toModelImport(String name) {
    if(namespaces.containsKey(name)) {
      return "using " + namespaces.get(name) + ";";
    }
    else if(systemIncludes.contains(name)) {
      return "#include <" + name + ">";
    }
    return "#include \"" + name + ".h\"";
  }

  /**
   * Escapes a reserved word as defined in the `reservedWords` array. Handle escaping
   * those terms here.  This logic is only called if a variable matches the reseved words
   * 
   * @return the escaped term
   */
  @Override
  public String escapeReservedWord(String name) {
    return "_" + name;  // add an underscore to the name
  }

  /**
   * Location to write model files.  You can use the modelPackage() as defined when the class is
   * instantiated
   */
  public String modelFileFolder() {
    return outputFolder + "/" + sourceFolder + "/" + modelPackage().replace('.', File.separatorChar);
  }

  /**
   * Location to write api files.  You can use the apiPackage() as defined when the class is
   * instantiated
   */
  @Override
  public String apiFileFolder() {
    return outputFolder + "/" + sourceFolder + "/" + apiPackage().replace('.', File.separatorChar);
  }

  @Override
  public String toModelFilename(String name) {
    return PREFIX + initialCaps(name);
  }

  @Override
  public String toApiFilename(String name) {
    return PREFIX + initialCaps(name) + "Api";
  }

  /**
   * Optional - type declaration.  This is a String which is used by the templates to instantiate your
   * types.  There is typically special handling for different property types
   *
   * @return a string value used as the `dataType` field for model templates, `returnType` for api templates
   */
  @Override
  public String getTypeDeclaration(Property p) {
    String swaggerType = getSwaggerType(p);

    if(p instanceof ArrayProperty) {
      ArrayProperty ap = (ArrayProperty) p;
      Property inner = ap.getItems();
      return getSwaggerType(p) + "<" + getTypeDeclaration(inner) + ">*";
    }
    else if (p instanceof MapProperty) {
      MapProperty mp = (MapProperty) p;
      Property inner = mp.getAdditionalProperties();
      return getSwaggerType(p) + "<String, " + getTypeDeclaration(inner) + ">*";
    }
    if(foundationClasses.contains(swaggerType))
      return swaggerType + "*";
    else if(languageSpecificPrimitives.contains(swaggerType))
      return toModelName(swaggerType);
    else
      return swaggerType + "*";
  }

  @Override
  public String toDefaultValue(Property p) {
    if(p instanceof StringProperty)
      return "new QString(\"\")";
    else if (p instanceof BooleanProperty)
      return "false";
    else if(p instanceof DateProperty)
      return "NULL";
    else if(p instanceof DateTimeProperty)
      return "NULL";
    else if (p instanceof DoubleProperty)
      return "0.0";
    else if (p instanceof FloatProperty)
      return "0.0f";
    else if (p instanceof IntegerProperty)
      return "0";
    else if (p instanceof LongProperty)
      return "0L";
    else if (p instanceof DecimalProperty)
      return "0.0";
    else if (p instanceof MapProperty) {
      MapProperty ap = (MapProperty) p;
      String inner = getSwaggerType(ap.getAdditionalProperties());
      return "NULL";
    }
    else if (p instanceof ArrayProperty) {
      ArrayProperty ap = (ArrayProperty) p;
      String inner = getSwaggerType(ap.getItems());
      if(!languageSpecificPrimitives.contains(inner)) {
        inner += "*";
      }
      return "new QList<" + inner + ">()";
    }
    // else
    if(p instanceof RefProperty) {
      RefProperty rp = (RefProperty) p;
      return "new " + toModelName(rp.getSimpleRef()) + "()";
    }
    return "NULL";
  }


  /**
   * Optional - swagger type conversion.  This is used to map swagger types in a `Property` into 
   * either language specific types via `typeMapping` or into complex models if there is not a mapping.
   *
   * @return a string value of the type or complex model for this property
   * @see com.wordnik.swagger.models.properties.Property
   */
  @Override
  public String getSwaggerType(Property p) {
    String swaggerType = super.getSwaggerType(p);
    String type = null;
    if(typeMapping.containsKey(swaggerType)) {
      type = typeMapping.get(swaggerType);
      if(languageSpecificPrimitives.contains(type))
        return toModelName(type);
      if(foundationClasses.contains(type))
        return type;
    }
    else
      type = swaggerType;
    return toModelName(type);
  }

  @Override
  public String toModelName(String type) {
    if(typeMapping.keySet().contains(type) ||
      typeMapping.values().contains(type) || 
      importMapping.values().contains(type) ||
      defaultIncludes.contains(type) ||
      languageSpecificPrimitives.contains(type)) {
      return type;
    }
    else {
      return PREFIX + Character.toUpperCase(type.charAt(0)) + type.substring(1);
    }
  }

  @Override
  public String toApiName(String type) {
    return PREFIX + Character.toUpperCase(type.charAt(0)) + type.substring(1) + "Api";
  }
}