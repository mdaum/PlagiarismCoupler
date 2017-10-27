package model;

public class StudentPair {
	
	Student left;
	Student right;
	float rawScore;
	float relativeScore;
	String id;
	
	public StudentPair(Student left, Student right, float rawScore, float relativeScore){
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

	public float getRawScore() {
		return rawScore;
	}

	public float getRelativeScore() {
		return relativeScore;
	}

	public String getId() {
		return id;
	}

}
