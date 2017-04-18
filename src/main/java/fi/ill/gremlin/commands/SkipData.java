package fi.ill.gremlin.commands;

import net.dv8tion.jda.core.entities.Member;

import java.util.List;

/**
 * @author haze
 * @since 3/26/2017
 */
public class SkipData {
    private Integer votesLeft;
    private List<Member> voters;

    public SkipData(Integer votesLeft, List<Member> voters) {

        this.votesLeft = votesLeft;
        this.voters = voters;
    }

    public Integer getVotesLeft() {
        return votesLeft;
    }

    public void setVotesLeft(Integer votesLeft) {
        this.votesLeft = votesLeft;
    }

    @Override
    public String toString() {
        return "SkipData{" +
                "votesLeft=" + votesLeft +
                ", voters=" + voters +
                '}';
    }

    public List<Member> getVoters() {
        return voters;
    }

    public void setVoters(List<Member> voters) {
        this.voters = voters;
    }
}
