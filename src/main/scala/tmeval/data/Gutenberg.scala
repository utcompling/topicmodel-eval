package tmeval.data

import java.io._
import org.apache.commons.vfs2.{VFS,FileObject}
import tmeval.Constants.TMEVAL_DIR
import CorpusUtil.writeString

/**
 * Convert the Gutenberg books in the supplied bzip2 archive into "documents" of
 * a specified length (default = 1000 tokens).
 *
 * The documents come from those collected for Kumar, Baldridge, Lease, and Ghosh (2012)
 * "Dating Texts without Explicit Temporal Cues", available here:
 *
 *   http://arxiv.org/abs/1211.2290
 *
 * @author jasonbaldridge 
 */ 
object GutenbergPreparer {
  
  val tmevalDir = new File(TMEVAL_DIR)
  val outputDir = new File(tmevalDir, "data/extracted/gutenberg")

  def main(args: Array[String]) {
    extract()
  }

  def extract(length: Int = 1000, minLength: Int = 100) {

    println("Extracting Gutenberg data.")
    val fileDescr = "tbz2:file:/" + tmevalDir.getAbsolutePath + "/data/orig/gutenberg.tar.bz2"
    val topDir = VFS.getManager.resolveFile(fileDescr).getChild("gutenberg")
    process(topDir.getChild("train"), new File(outputDir, "train"), length, minLength)
    process(topDir.getChild("eval"), new File(outputDir, "eval"), length, minLength)
  }

  def process(raw: FileObject, outputDir: File, targetLength: Int, minLength: Int) {
    println("Processing files in " + raw)
    outputDir.mkdirs

    for (gutenbergFile <- raw.getChildren if gutenbergFile.getName.getBaseName.endsWith("_clean.txt")) {
      val name = gutenbergFile.getName.getBaseName
      val outputName = name.replaceAll(" ","-")
      println("** " + name)

      var currentDocument = new StringBuffer
      var numWordsThisDoc = 0
      var subdocumentId = 0

      // Use latin1 so that we don't get invalid character exception.
      // See: http://stackoverflow.com/questions/7280956/how-to-skip-invalid-characters-in-stream-in-java-scala

      val gutfileInputStream = gutenbergFile.getContent.getInputStream
           
      io.Source.fromInputStream(gutfileInputStream, "latin1").getLines.foreach { line => {
        numWordsThisDoc += line.replaceAll("""\s+"""," ").split(" ").length
        currentDocument.append(line).append('\n')
        if (numWordsThisDoc > targetLength) {
          writeString(outputDir, outputName+"-"+subdocumentId, currentDocument.toString)
          currentDocument = new StringBuffer
          numWordsThisDoc = 0
          subdocumentId += 1
        } 
      }}

      // Only write the last document if it has at least minLength words.
      if (numWordsThisDoc > minLength)
        writeString(outputDir, outputName+"-"+subdocumentId, currentDocument.toString)
    }
  }

}
