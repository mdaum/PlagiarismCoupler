package model;

public class StudentPair {
	
	Student left;
	Student right;
	double rawScore;
	double relativeScore;
	String id;
	
	public StudentPair(Student left, Student right, double rawScore, double relativeScore){
		this.left=left;
		this.right=right;
		this.rawScore=rawScore;
		this.relativeScore=relativeScore;
		this.id = left.getId()+"|"+right.getId(); //concat both identifiers for the students, should be unique
	}
	
	public Student getLeft() {
		return left;
	}

	public Student getRight() {
		return right;
	}

	public double getRawScore() {
		return rawScore;
	}

	public double getRelativeScore() {
		return relativeScore;
	}

	public String getId() {
		return id;
	}

}
