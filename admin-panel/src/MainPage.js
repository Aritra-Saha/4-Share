import React from "react";
import CardList from "./CardList";
import { makeStyles } from "@material-ui/core/styles";
import Grid from "@material-ui/core/Grid";
import AppBar from "@material-ui/core/AppBar";
import Toolbar from "@material-ui/core/Toolbar";
import Typography from "@material-ui/core/Typography";
import Container from "@material-ui/core/Container";

const useStyles = makeStyles(theme => ({
  root: {
    flexGrow: 1
  },
  title: {
    flexGrow: 1
  },
  appBar: {
    backgroundColor: "indigo"
  }
}));

function FormRow(props) {
  const items = [];
  for (var i = 0; i < props.countries.length; i++) {
    items.push(
      <Grid
        key={i.toString()}
        item
        xs={4}
        container
        spacing={0}
        direction="column"
        justify="center"
        style={{ minHeight: "50vh" }}
      >
        <CardList
          country={props.countries[i].country}
          currency={props.countries[i].currency}
          image={props.countries[i].image}
        />
      </Grid>
    );
  }

  return <React.Fragment>{items}</React.Fragment>;
}

function FormInfo(props) {
  const classes = useStyles();

  const countries = props.countries;

  countries.sort((a, b) => (a.country > b.country ? 1 : -1));

  const rows = [];
  let key = 0;
  for (var i = 0; i < countries.length; i = i + 3) {
    rows.push(
      <Grid key={"row" + key.toString()} container item xs={12} spacing={3}>
        <FormRow countries={countries.slice(i, i + 3)} />
      </Grid>
    );
    key++;
  }

  return (
    <div className={classes.root}>
      <Container>
        <Grid container spacing={1}>
          {rows}
        </Grid>
      </Container>
    </div>
  );
}

function MainBar() {
  const classes = useStyles();

  return (
    <div className={classes.root}>
      <AppBar position="static" className={classes.appBar}>
        <Toolbar>
          <Typography variant="h6" className={classes.title}>
            Cash Calculator Administration
          </Typography>
        </Toolbar>
      </AppBar>
    </div>
  );
}

class MainPage extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      countries: props.countries
    };
  }

  render() {
    return (
      <div>
        <MainBar />
        <FormInfo countries={this.state.countries} />
      </div>
    );
  }
}

export default MainPage;