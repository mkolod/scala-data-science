package us.marek.datascience

import edu.arizona.sista.processors.CorefMention
import edu.arizona.sista.processors.corenlp.CoreNLPProcessor
import edu.arizona.sista.processors.fastnlp.FastNLPProcessor
import edu.arizona.sista.struct.DirectedGraph

object SistaCoreNlpDemo extends App {

  val proc = new CoreNLPProcessor(withDiscourse = true)
  val text = "John Smith went to China. He visited Beijing on January 10th 2013."
  val doc = proc.annotate(text)

    doc.sentences
         .map {
          x =>
            s"""
               |Sentence: ${x.getSentenceText}
               |Tokens: ${x.words.mkString(",")}
               |Start character offsets: ${x.startOffsets.mkString(",")}
               |End character offsets: ${x.endOffsets.mkString(",")}
               |${ x.lemmas match {
                     case Some(lemmas) => s"Lemmas: ${lemmas.toSeq}"
                     case None => ""
                   }
                 }
               |${ x.entities match {
                     case Some(entities) => s"Entities: ${entities.toSeq}"
                     case None => ""
                   }
                 }
               |${ x.norms match {
                     case Some(norms) => s"Normalized entities: ${x.norms.get.mkString(",")}"
                     case None => ""
                   }
                 }
               |${x.syntacticTree match {
                    case Some(tree) => s"Syntactic tree: $tree"
                    case None => ""
                  }
                }
            """.stripMargin

          }
        .foreach(println)


  doc.coreferenceChains.foreach {
    chains =>
      chains.getChains.flatMap {
        chain =>
          if (chain.size > 1)
            Some(s"""
                     |Found coreference chain:
                     |${chain.map {
                          mention =>
                            s"""
                               |headIndex: ${mention.headIndex}
                               |startTokenOffset: ${mention.startOffset}
                               |endTokenOffset: ${mention.endOffset}
                               |text: ${
                                  doc.
                                    sentences(mention.sentenceIndex).
                                    words.
                                    slice(mention.startOffset, mention.endOffset).
                                    mkString("[", " ", "]")
                                }
                               |
                             """.stripMargin
                         }.mkString("\n")
                       }
                     |
                     |""".stripMargin)
          else
            None
      }.foreach(println)
  }

}
