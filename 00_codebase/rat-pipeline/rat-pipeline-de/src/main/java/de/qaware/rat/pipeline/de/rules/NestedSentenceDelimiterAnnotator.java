package de.qaware.rat.pipeline.de.rules;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.qaware.rat.api.models.RuleParameter;
import de.qaware.rat.common.UimaUtils;
import de.qaware.rat.statistics.TextStatistic;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * This {@code Java Annotator} class detects nested sentences based on the
 * occurrences of delimiters in a sentence.
 * 
 * @author Matthias
 *
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" }, outputs = {
                "de.qaware.rat.type.RatReadabilityAnomaly" })
public class NestedSentenceDelimiterAnnotator extends JCasAnnotator_ImplBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(NestedSentenceConjunctionAnnotator.class);

    private static final List<String> DELIMITERS = Arrays.asList(",", ":", ";", "–");

    public static final String DELIMITERS_IN_SENTENCE = RuleParameter.THRESHOLD;
    @ConfigurationParameter(name = RuleParameter.THRESHOLD, mandatory = false, defaultValue = "3")
    protected int delimitersInSentence;

    public static final String SEVERITY = RuleParameter.SEVERITY;
    @ConfigurationParameter(name = RuleParameter.SEVERITY, mandatory = true, defaultValue = "Minor")
    protected String severity;

    @Override
    public void process(JCas aJCas) {
        LOGGER.info("Start NestedSentenceDelimiterAnnotator");

        Collection<Sentence> sentences = JCasUtil.select(aJCas, Sentence.class);

        for (Sentence sentence : sentences) {
            List<Token> matches = TextStatistic.getMatchesInSentence(aJCas, sentence, DELIMITERS);

            if (matches.size() >= delimitersInSentence) {
                UimaUtils.createRatReadabilityAnomaly(aJCas, "NestedSentence", "ReadabilityAnomaly", severity,
                        "Dieser Satz enthät " + delimitersInSentence + " oder mehr (" + matches.size()
                                + ") Trennzeichen. Verwenden Sie eine Interpunktion und teilen sie den Satz auf.",
                        matches.size(), matches.get(0).getBegin(), matches.get(0).getEnd());
            }
        }
    }
}