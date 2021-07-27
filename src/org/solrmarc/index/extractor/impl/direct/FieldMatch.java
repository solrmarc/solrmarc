package org.solrmarc.index.extractor.impl.direct;

import java.util.Collection;

import org.marc4j.marc.VariableField;
import org.solrmarc.index.specification.Specification;


public class FieldMatch
{
    VariableField vf;
    Specification spec;

    public FieldMatch(VariableField vf, Specification spec)
    {
        this.vf = vf;
        this.spec = spec;
    }

    public VariableField getVf()
    {
        return vf;
    }

    public void setVf(VariableField vf)
    {
        this.vf = vf;
    }

    public Specification getSpec()
    {
        return spec;
    }

    public void setSpec(Specification spec)
    {
        this.spec = spec;
    }

    public void addValuesTo(Collection<String> results) throws Exception
    {
        spec.addFieldValues(results, vf);
    }

    // static Collection<String> collect(Collection<FieldMatch> matches)
    // {
    // Collection<String> result = matches.
    // for (FieldMatch fm : matches)
    // {
    // SingleSpecification spec = fm.getSpec();
    // VariableField vf = fm.getVf();
    // spec.addFieldValues(result, vf);
    // }
    // return result;
    // }
    // /// added for testing parser and conditionals
    // public String toString(String separator, String sfTagPattern, boolean
    // cleanAll, boolean cleanEnd, String translationMapName)
    // {
    // if (vf instanceof ControlField)
    // {
    // String data = ((ControlField) vf).getData();
    // String s1 = this.spec.subfields;
    // if (s1 == null || s1.length() == 0) return(data);
    // int offset = Integer.parseInt(s1.replaceAll("\\[([0-9]+)(-[0-9]+)\\]",
    // "$1"));
    // String endOffsetStr = s1.replaceAll("\\[([0-9]+-)([0-9]+)\\]", "$2");
    // int endOffset = offset;
    // if (endOffsetStr != null) endOffset = Integer.parseInt(endOffsetStr);
    // if (data.length() < offset) return("");
    // String posVal = data.substring(offset, endOffset+1);
    // if (translationMapName != null)
    // {
    // TranslationMap map =
    // TranslationMapFactory.theMaps.findMapFromSpec(translationMapName);
    // if (map != null)
    // posVal = map.remap(posVal);
    // }
    // return(posVal);
    // }
    // else
    // {
    // StringBuffer buffer = new StringBuffer("");
    // Pattern subfieldPattern = Pattern.compile(spec.subfields.length() == 0 ?
    // "." : spec.subfields);
    // DataField marcField = (DataField) vf;
    // List<Subfield> subfields = marcField.getSubfields();
    // for (Subfield subfield : subfields)
    // {
    // String codeStr = "" + subfield.getCode();
    // Matcher matcher = subfieldPattern.matcher(codeStr);
    // if (matcher.matches())
    // {
    // if (buffer.length() > 0)
    // buffer.append(separator != null ? separator : " ");
    // if (sfTagPattern != null)
    // {
    // String tagPattern = sfTagPattern.replaceAll("[?]", codeStr);
    // buffer.append(tagPattern);
    // }
    // buffer.append(cleanAll ? Utils.cleanData(subfield.getData().trim()) :
    // subfield.getData().trim());
    // }
    // }
    // String result = cleanEnd ? Utils.cleanData(buffer.toString()) :
    // buffer.toString();
    // if (translationMapName != null && result != null)
    // {
    // TranslationMap map =
    // TranslationMapFactory.theMaps.findMapFromSpec(translationMapName);
    // if (map != null)
    // result = map.remap(result);
    // }
    // return(result);
    // }
    // }

    // public Collection<String> toStringSet(String separator, String
    // sfTagPattern, boolean cleanAll, boolean cleanEnd)
    // {
    // if (vf instanceof ControlField)
    // {
    // String data = ((ControlField) vf).getData();
    // String s1 = this.spec.subfields;
    // if (s1 == null || s1.length() == 0) return(data);
    // int offset = Integer.parseInt(s1.replaceAll("\\[([0-9]+)(-[0-9]+)\\]",
    // "$1"));
    // String endOffsetStr = s1.replaceAll("\\[([0-9]+-)([0-9]+)\\]", "$2");
    // int endOffset = offset;
    // if (endOffsetStr != null) endOffset = Integer.parseInt(endOffsetStr);
    // if (data.length() < offset) return("");
    // String posVal = data.substring(offset, endOffset+1);
    // return(posVal);
    // }
    // else
    // {
    // StringBuffer buffer = new StringBuffer("");
    // Pattern subfieldPattern = Pattern.compile(spec.subfields.length() == 0 ?
    // "." : spec.subfields);
    // DataField marcField = (DataField) vf;
    // List<Subfield> subfields = marcField.getSubfields();
    // for (Subfield subfield : subfields)
    // {
    // String codeStr = "" + subfield.getCode();
    // Matcher matcher = subfieldPattern.matcher(codeStr);
    // if (matcher.matches())
    // {
    // if (buffer.length() > 0)
    // buffer.append(separator != null ? separator : " ");
    // if (sfTagPattern != null)
    // {
    // String tagPattern = sfTagPattern.replaceAll("[?]", codeStr);
    // buffer.append(tagPattern);
    // }
    // buffer.append(cleanAll ? Utils.cleanData(subfield.getData().trim()) :
    // subfield.getData().trim());
    // }
    // }
    // if (buffer.length() > 0)
    // return(cleanEnd ? Utils.cleanData(buffer.toString()) :
    // buffer.toString());
    // else
    // return(null);
    // }
    // }

}
