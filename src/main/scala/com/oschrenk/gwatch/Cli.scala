package com.oschrenk.gwatch

import java.io.File

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.AnyObjectId
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.SymbolicRef
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.revwalk.RevCommit

object Formatters {
   val default: (RevCommit) => String = (rev: RevCommit) => {
     rev.toString
   }

   val oneline: (RevCommit) => String = (rev: RevCommit) => {
     val id = rev.toObjectId.abbreviate(7).name
     val short = rev.getShortMessage
     val author = rev.getAuthorIdent
     val time = author.getWhen
     val name = author.getName

     s"$id - $short ($time) <$name>"
   }

   def formatRefs(refs: Set[Ref]): String = {
     refs.map(format).mkString("(", ",", ") ")
   }

   def format(ref: Ref): String = {
     ref match {
       case ref: SymbolicRef => s"${ref.getName} -> ${format(ref.getTarget)}"
       case r => s"${r.getName.replace("refs/heads/", "")}"
     }
   }

   def decorated(allsRefs: Map[AnyObjectId, Set[Ref]]): (RevCommit) => String = (rev: RevCommit) => {
     val id = rev.toObjectId.abbreviate(7).name
     val short = rev.getShortMessage
     val author = rev.getAuthorIdent
     val time = author.getWhen
     val name = author.getName
     val commitRefs = allsRefs.get(rev)
     val refs = commitRefs match {
       case Some(refs) => formatRefs(refs)
       case None => ""
     }

     s"$id $refs$short"
   }
}

object Cli extends App {

  import scala.collection.JavaConverters._

  val workingDir = new File(".").getAbsolutePath
  val repoDir = new File(workingDir, ".git").getCanonicalFile

  val repo = new FileRepositoryBuilder().setGitDir(repoDir)
    .readEnvironment() // scan environment GIT_* variables
    .findGitDir()      // scan up the file system tree
    .build()
  val git = new Git(repo)

  println("Having repository: " + repo.getDirectory)

  // the Ref holds an ObjectId for any type of object (tree, commit, blob, tree)
  val head = repo.exactRef("refs/heads/master")
  println("Ref of refs/heads/master: " + head)

  println()
  val branches: Seq[Ref] = asScalaBuffer(git.branchList().call())
  branches.foreach { ref =>
    println(s"Branch: ${ref.getName} ${ref.getObjectId.getName}")
  }

  println()
  val commits = git.log().all().call().asScala

  val allsRefs = git.getRepository().getAllRefsByPeeledObjectId().asScala.mapValues(_.asScala.toSet).toMap
  val format = Formatters.decorated(allsRefs)
  commits.foreach { rev =>
    println(format(rev))
  }

}
