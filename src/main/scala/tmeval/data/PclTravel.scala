package tmeval.data

import java.io._
import org.apache.commons.vfs2.{VFS,FileObject}
import tmeval.Constants.TMEVAL_DIR
import CorpusUtil._

/**
 * Convert the PCL Travel corpus archive into extracted files for learning
 * and evaluating topic models.
 *
 * The native format of the documents is TEI-encoded XML. See the following
 * website for more information about the books:
 *
 *   http://www.lib.utexas.edu/books/travel/index.html
 *
 *  @author jasonbaldridge 
 */ 
object PclTravelPreparer {
  
  val tmevalDir = new File(TMEVAL_DIR)
  val outputDir = new File(tmevalDir, "data/extracted/pcl-travel")

  def main(args: Array[String]) {
    //val numParagraphsPerDoc = if (args.length > 0) args(0).toInt else 10
    //extractParagraphs(numParagraphsPerDoc)
    extractFullTexts
  }

  lazy val parser = getNonValidatingXmlParser

  lazy val pclArchive = 
    "tbz2:file:/" + tmevalDir.getAbsolutePath + "/data/orig/pcl-travel.tar.bz2"


  /**
   * Read the XML of each book (in the bzip2 archive) and for each one, get all the
   * paragraphs and output one file for every N paragraphs. This is necessary because
   * it isn't very useful to compute topic models where each document is a full book.
   * Better ways of segmenting documents can be performed, of course, but this produces
   * interesting topics and is simple.
   */ 
  def extractParagraphs(numParagraphsPerDocument: Int = 10) {

    println("Extracting PCL Travel data.")

    val topDir = VFS.getManager.resolveFile(pclArchive).getChild("pcl-travel")
    outputDir.mkdirs

    var bookId = 1
    for (bookFile <- topDir.getChildren if bookFile.getName.getBaseName.endsWith("xml")) {

      val trainOrEvalDirName = if (bookId.toInt < 65) "train" else "eval"
      val trainOrEvalDir = new File(outputDir, trainOrEvalDirName)
      trainOrEvalDir.mkdirs

      val bookName = bookFile.getName.getBaseName.dropRight(4)
      println("** " + bookName)

      val bookXml = xml.XML.withSAXParser(parser).load(bookFile.getContent.getInputStream)
      val bookText = (bookXml \ "text")
      val paragraphs = (bookText \\ "p")
      var subDocId = 1
      for (parSeq <- paragraphs.grouped(numParagraphsPerDocument)) {
        val doc = parSeq.map(_.text).mkString("\n")
        writeString(trainOrEvalDir, bookName+"-"+subDocId, doc)
        subDocId += 1
      }
      bookId += 1
    }
  }


  /**
   * Read the XML of each book (in the bzip2 archive) and for each one, get all the 
   * text and write it to a file. This isn't needed for the experiments in the paper,
   * but might be useful to someone who just wants to grab the raw texts of all the
   * books.
   */
  def extractFullTexts {
    val topDir = VFS.getManager.resolveFile(pclArchive).getChild("pcl-travel")
    val pclFullTextDir = new File("pcl-travel-full-text")
    pclFullTextDir.mkdirs

    for (bookFile <- topDir.getChildren if bookFile.getName.getBaseName.endsWith("xml")) {
      val bookName = bookFile.getName.getBaseName.dropRight(4)
      println("** " + bookName)
      val bookXml = xml.XML.withSAXParser(parser).load(bookFile.getContent.getInputStream)
      val bookText = (bookXml \ "text")
      writeString(pclFullTextDir, bookName+"-fulltext", bookText.text)
    }
  }

}
